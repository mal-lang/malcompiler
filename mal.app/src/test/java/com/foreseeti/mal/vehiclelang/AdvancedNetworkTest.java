package com.foreseeti.mal.vehiclelang;

import com.foreseeti.mal.MalTest;
import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import vehicle.CANNetwork;
import vehicle.ConnectionOrientedDataflow;
import vehicle.ConnectionlessDataflow;
import vehicle.ECU;
import vehicle.EthernetGatewayECU;
import vehicle.EthernetNetwork;
import vehicle.FirmwareUpdaterService;
import vehicle.GatewayECU;
import vehicle.IDPS;
import vehicle.J1939Network;
import vehicle.LINNetwork;
import vehicle.NetworkService;
import vehicle.SensorOrActuator;
import vehicle.TransmitterService;
import vehicle.VehicleNetwork;

public class AdvancedNetworkTest extends MalTest {

  @Test
  public void testDataflowWithFirewallAndIDPS() {
    /*
    TransmitterEcu <---> vNet1 <---> GatewayECU <---> vNet2 <---> ListenerECU
             |            |          (Firewall)        |
             |            |          (    +   )    Dataflow,
      Transmitter <--> Dataflow      (  IDPS  )  OtherDataFlow
     */
    // TARGET: OtherDataFlow.transmit ENTRY_POINT: vNet1.physicalAccess
    for (int i = 1; i <= 4; i++) {
      // CHANGE following variables to switch Firewall and IDPS on/off
      boolean firewallStatus = false;
      boolean idpsStatus = false;
      System.err.println("ITERATION " + i);
      System.out.println(
          "### " + Thread.currentThread().getStackTrace()[1].getMethodName() + ", iteration: " + i);
      switch (i) {
        case 1:
          firewallStatus = true;
          idpsStatus = true;
          break;
        case 2:
          firewallStatus = true;
          idpsStatus = false;
          break;
        case 3:
          firewallStatus = false;
          idpsStatus = true;
          break;
        case 4:
          firewallStatus = false;
          idpsStatus = false;
          break;
        default:
          break;
      }
      // Start of test
      ECU SrvEcu =
          new ECU(
              "TransmitterECU",
              true,
              true); // Enabled operation mode and message confliction protection on all ECUs.
      ECU ClnEcu = new ECU("ListenerECU", true, true);
      GatewayECU GateEcu = new GatewayECU("GatewayECU", true, true, firewallStatus);
      IDPS idps;
      VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
      VehicleNetwork vNet2 = new VehicleNetwork("vNet2");
      ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
      ConnectionlessDataflow otherDataflow = new ConnectionlessDataflow("OtherDataflow");
      TransmitterService service = new TransmitterService("Transmitter");

      if (firewallStatus) System.out.println("# Firewall protection is ON!");
      else System.out.println("# Firewall protection is OFF :/");
      if (idpsStatus) {
        System.out.println("# IDPS protection is ON!");
        idps = new IDPS("IDPS");
        GateEcu.addIdps(idps);
      } else System.out.println("# IDPS protection is OFF :/");

      SrvEcu.addVehiclenetworks(vNet1);
      ClnEcu.addVehiclenetworks(vNet2);
      GateEcu.addVehiclenetworks(vNet1);
      GateEcu.addVehiclenetworks(vNet2);

      SrvEcu.addExecutees(service);

      vNet1.addTrafficGatewayECU(GateEcu);
      vNet2.addTrafficGatewayECU(GateEcu);
      vNet1.addDataflows(dataflow);
      vNet2.addDataflows(dataflow);
      vNet2.addDataflows(otherDataflow);

      service.addDataflows(dataflow);

      Attacker attacker = new Attacker();
      attacker.addAttackPoint(vNet1.physicalAccess);
      attacker.attack();

      // For debugging only:
      // System.out.println("# FirewallProtection: " + GateEcu.FirewallProtection.isEnabled());
      // System.out.println("# idpsExists 'defense': " + GateEcu.idpsExists.isEnabled());
      // System.out.println("# idpsDoesNotExist 'defense': " +
      // GateEcu.idpsDoesNotExist.isEnabled());

      GateEcu.forwarding.assertCompromisedInstantaneously();

      vNet1.accessNetworkLayer.assertCompromisedInstantaneously();
      vNet1.messageInjection.assertCompromisedInstantaneously();
      vNet1.eavesdrop.assertCompromisedInstantaneously();
      vNet1.denialOfService.assertCompromisedInstantaneously();

      SrvEcu._networkServiceMessageInjection
          .assertUncompromised(); // Because message confliction protection is enabled

      // if (i != 4)
      dataflow.denialOfService.assertCompromisedInstantaneously();
      dataflow.eavesdrop.assertCompromisedInstantaneously();

      dataflow.transmit.assertCompromisedWithEffort();
      // else
      //    dataflow.transmit.assertCompromisedInstantaneously(); // This happens only if Firewall +
      // IDPS are disabled. Must rethink about it!

      GateEcu.forwarding.assertCompromisedInstantaneously();
      if (firewallStatus) { // Firewall is enabled
        GateEcu.bypassFirewall.assertUncompromised();
        if (idpsStatus) { // IDPS should not be bypassed by any way
          GateEcu.gatewayNoIDPS.assertUncompromised();
          GateEcu.gatewayBypassIDPS.assertUncompromised();
        } else { // IDPS is off but firewall is on so it should not be bypassed by any way
          GateEcu.gatewayNoIDPS.assertUncompromised();
          GateEcu.gatewayBypassIDPS.assertUncompromised();
        }
      } else { // Firwall is disabled
        GateEcu.bypassFirewall.assertCompromisedInstantaneously();
        if (idpsStatus) { // Only bypass should work
          GateEcu.gatewayNoIDPS.assertUncompromised();
          GateEcu.gatewayBypassIDPS.assertCompromisedInstantaneously();
        } else { // Only NoIDPS should work
          GateEcu.gatewayNoIDPS.assertCompromisedInstantaneously();
          GateEcu.gatewayBypassIDPS.assertUncompromised();
        }
      }
      if (firewallStatus) { // This, ideally, should be uncompromished only when Firewall is active.
        GateEcu.access.assertUncompromised();
        otherDataflow.transmit.assertUncompromised();
        vNet2.messageInjection.assertUncompromised();
      } else if (!firewallStatus && idpsStatus) {
        otherDataflow.transmit.assertCompromisedWithEffort();
        vNet2.messageInjection.assertUncompromised();
      } else if (!firewallStatus && !idpsStatus) {
        otherDataflow.transmit.assertCompromisedWithEffort();
        vNet2.messageInjection.assertCompromisedInstantaneously();
      }

      // Below lines are needed because I am iterating the same model inside one @test.
      Asset.allAssets.clear();
      AttackStep.allAttackSteps.clear();
      Defense.allDefenses.clear();
    }
  }

  @Test
  public void testCANwithGatewayECU() {
    // Testing attacks on CAN networks connected with Gateway ECU.
    /*
        Ecu#1 <---> vNet(CAN) <---> GatewayECU <---> vNet2(CAN) <---> Ecu#2
    */
    // TARGET: vNet2._networkSpecificAttack
    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name
    ECU Ecu1 =
        new ECU("ECU#1", true, true); // Enabled operation mode and message confliction protection
    ECU Ecu2 = new ECU("ECU#2", true, true);
    GatewayECU GateEcu = new GatewayECU("GatewayECU", true, true, true); // Enabled all defenses
    CANNetwork vNet1 = new CANNetwork("vNet1");
    CANNetwork vNet2 = new CANNetwork("vNet2");
    ConnectionlessDataflow dataflow1 = new ConnectionlessDataflow("Dataflow1");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);
    GateEcu.addTrafficVNetworks(vNet1);
    GateEcu.addTrafficVNetworks(vNet2);
    vNet1.addDataflows(dataflow1);
    vNet2.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(vNet1.accessNetworkLayer);
    attacker.attack();

    vNet1._networkSpecificAttack.assertCompromisedInstantaneously();
    vNet1.busOffAttack.assertCompromisedWithEffort();

    vNet2._networkSpecificAttack.assertUncompromised();
    vNet2.busOffAttack.assertUncompromised();
  }

  @Test
  public void testUDSwithGatewayECU() {
    // Testing forwarding of UDS messages over a Gateway ECU.
    /*
         Ecu#1 <---> vNet <---> GatewayECU <---> vNet2 <---> Ecu#2
           |
      FirmwareUpdaterService
    */
    // TARGET: UDS firmware upload on connected networks
    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name
    boolean firewallStatus = false;
    ECU Ecu1 =
        new ECU("ECU#1", true, true); // Enabled operation mode and message confliction protection
    ECU Ecu2 = new ECU("ECU#2", true, true);
    GatewayECU GateEcu =
        new GatewayECU("GatewayECU", true, true, firewallStatus); // Enabled all defenses
    // IDPS idps = new IDPS("idps");
    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");
    FirmwareUpdaterService fwUpdater =
        new FirmwareUpdaterService("FirmwareUpdater", false); // Turned off UDS SecurityAccess

    Ecu1.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);
    GateEcu.addTrafficVNetworks(vNet1);
    GateEcu.addTrafficVNetworks(vNet2);
    // GateEcu.addIdps(idps);
    Ecu1.addFirmwareUpdater(fwUpdater);
    vNet1.addNetworkFwUpdater(fwUpdater);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(GateEcu.access);
    attacker.attack();

    vNet1.accessUDSservices.assertCompromisedInstantaneously();
    vNet2.accessUDSservices.assertCompromisedInstantaneously();
    fwUpdater.access.assertCompromisedInstantaneously();
    Ecu1.udsFirmwareModification.assertCompromisedInstantaneously();
    Ecu2.udsFirmwareModification.assertUncompromised();
  }

  @Test
  public void testAccessEthGatewayECU() {
    // Testing access on an Ethernet Gateway ECU.
    /*
       Ecu#1 <---> vNet(CAN) <---> EthGatewayECU <---> Ethernet <---> Dataflow#2
                      | |
                      | |---> Ecu#2 <---> Sensor/Actuator
                      |
                   Dataflow
    */
    // TARGET: dataflow.transmit & dataflow2.respond ENTRY_POINT: EthGatewayECU.access
    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name
    ECU Ecu1 =
        new ECU("ECU#1", true, true); // Enabled operation mode and message confliction protection
    ECU Ecu2 = new ECU("ECU#2", true, true);
    SensorOrActuator PhyMachine = new SensorOrActuator("Sensor/Actuator");
    EthernetGatewayECU EthGateEcu =
        new EthernetGatewayECU("EthGatewayECU", true, true, true); // Enabled firewall
    CANNetwork vNet = new CANNetwork("CAN");
    EthernetNetwork ethNet = new EthernetNetwork("Ethernet");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
    ConnectionOrientedDataflow dataflow2 = new ConnectionOrientedDataflow("Dataflow2");

    Ecu1.addVehiclenetworks(vNet);
    Ecu2.addVehiclenetworks(vNet);
    PhyMachine.addHardwarePlatform(Ecu2);
    EthGateEcu.addTrafficVNetworks(vNet);
    EthGateEcu.addTrafficNetworks(ethNet);
    vNet.addDataflows(dataflow);
    ethNet.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(EthGateEcu.access);
    attacker.attack();

    vNet.manInTheMiddle.assertCompromisedInstantaneously();
    ethNet.manInTheMiddle.assertCompromisedInstantaneously();

    dataflow.transmit.assertCompromisedInstantaneously();
    dataflow2.respond.assertCompromisedInstantaneously();
  }

  @Test
  public void testNetworkAccessWithEthGatewayECU() {
    // Testing access on an Ethernet Gateway ECU.
    /*
       Ecu#1 <---> vNet(CAN) <---> EthGatewayECU <---> Ethernet <---> Dataflow#2
                      | |                                  |
                      | |---> Ecu#2 <---> Sensor/Actuator  |---> NetworkService
                      |
                   Dataflow
    */
    // TARGET: dataflow.transmit & dataflow2.respond ENTRY_POINT: vNet.physicalAccess
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    boolean firewallStatus = false;
    ECU Ecu1 =
        new ECU("ECU#1", true, true); // Enabled operation mode and message confliction protection
    ECU Ecu2 = new ECU("ECU#2", true, true);
    SensorOrActuator PhyMachine = new SensorOrActuator("Sensor/Actuator");
    EthernetGatewayECU EthGateEcu =
        new EthernetGatewayECU("EthGatewayECU", firewallStatus, true, true);
    CANNetwork vNet = new CANNetwork("CAN");
    EthernetNetwork ethNet = new EthernetNetwork("Ethernet");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
    ConnectionOrientedDataflow dataflow2 = new ConnectionOrientedDataflow("Dataflow2");
    NetworkService netService = new NetworkService("NetworkService");

    Ecu1.addVehiclenetworks(vNet);
    Ecu2.addVehiclenetworks(vNet);
    PhyMachine.addHardwarePlatform(Ecu2);
    EthGateEcu.addTrafficVNetworks(vNet);
    EthGateEcu.addTrafficNetworks(ethNet);
    vNet.addDataflows(dataflow);
    ethNet.addDataflows(dataflow2);
    // ethNet.addNetworkServices(netService);
    netService.addNetworks(ethNet);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(vNet.physicalAccess);
    attacker.attack();

    EthGateEcu.bypassFirewall.assertCompromisedInstantaneously();
    ethNet.accessNetworkLayer.assertCompromisedInstantaneously();
    vNet.accessNetworkLayer.assertCompromisedInstantaneously();

    dataflow.transmit.assertCompromisedWithEffort();
    dataflow2.request.assertUncompromised();
    dataflow2.respond.assertUncompromised();
    netService.connect.assertCompromisedInstantaneously();
  }

  @Test
  public void testGainLINaccess() {
    // Testing access on LIN bus from a compromised CANbus ECU.
    /*
       Ecu#1 <---> vNet1(CAN)
              |
              ---> vNet2(LIN) <---> Ecu#2 <---> Sensor/Actuator
                      |
                   Dataflow
    */
    // TARGET: dataflow.maliciousRespond ENTRY_POINT: Ecu#1.access
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    ECU Ecu1 =
        new ECU("ECU#1", true, true); // Enabled operation mode and message confliction protection
    ECU Ecu2 = new ECU("ECU#2", true, true);
    CANNetwork vNet1 = new CANNetwork("CAN");
    LINNetwork vNet2 = new LINNetwork("LIN");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addVehiclenetworks(vNet2);
    Ecu2.addVehiclenetworks(vNet2);
    vNet2.addDataflows(dataflow);
    Attacker attacker = new Attacker();
    attacker.addAttackPoint(Ecu1.access);
    attacker.attack();

    vNet2.accessNetworkLayer.assertCompromisedInstantaneously();
    vNet2.messageInjection.assertCompromisedInstantaneously();
    dataflow.transmit.assertCompromisedWithEffort();
  }

  @Test
  public void paperTest() {
    // Unlocking and starting a car through a compromised BCM
    /*
    (Un)LockService <---> StartEngineDataflow
        |     |           |
        |    BCM <---> CAN bus <---> ECM
        |     |
        |     ---> LIN bus <---> Door_Locks(Actuator)
        |             |
        ----> UnlockDataflow

    Naming:
    BCM = Body Control Module
    ECM = Engine Control Unit
     */
    // TARGET: unlockDataflow & StartEngineDataflow maliciousRespond ENTRY_POINT:
    // BCM.bypassAccessControl
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    ECU bcm = new ECU("BCM", true, true);
    ECU ecm = new ECU("ECM", true, true);
    TransmitterService lockService = new TransmitterService("LockService");
    CANNetwork can = new CANNetwork("CAN");
    LINNetwork lin = new LINNetwork("LIN");
    ConnectionlessDataflow doorsDataflow = new ConnectionlessDataflow("unlockDataflow");
    ConnectionlessDataflow engineDataflow = new ConnectionlessDataflow("startEngineDataflow");

    bcm.addVehiclenetworks(can);
    bcm.addVehiclenetworks(lin);
    ecm.addVehiclenetworks(can);
    bcm.addExecutees(lockService);
    lockService.addDataflows(doorsDataflow);
    lockService.addDataflows(engineDataflow);
    can.addDataflows(engineDataflow);
    lin.addDataflows(doorsDataflow);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(bcm.bypassAccessControl);
    attacker.attack();

    lin.accessNetworkLayer.assertCompromisedInstantaneously();
    lin.messageInjection.assertCompromisedInstantaneously();
    doorsDataflow.maliciousTransmit.assertCompromisedInstantaneously();
    engineDataflow.maliciousTransmit.assertCompromisedInstantaneously();
  }

  @Test
  public void testJ1393Network() {
    // Testing access on J1939 network by uploading custom firmware on a CAN-bus ECU.
    /*
          Ecu#1 <---> vNet1(CAN)
           |
           ---> vNet2(J1393) <---> Ecu#2 <---> Sensor/Actuator
                    |
                 Dataflow
    */
    // TARGET: dataflow.maliciousRespond ENTRY_POINT: Ecu#1.access
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    boolean noFullJ1939Support = false;
    ECU Ecu1 = new ECU("ECU#1", false, true); // Enabled message confliction protection
    ECU Ecu2 = new ECU("ECU#2", false, true);
    CANNetwork vNet1 = new CANNetwork("CAN");
    J1939Network vNet2 = new J1939Network("J1939", noFullJ1939Support, false);
    ConnectionOrientedDataflow dataflow = new ConnectionOrientedDataflow("Dataflow");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addVehiclenetworks(vNet2);
    Ecu2.addVehiclenetworks(vNet2);
    vNet2.addJ1939dataflows(dataflow);
    Attacker attacker = new Attacker();
    attacker.addAttackPoint(Ecu1.access);
    attacker.addAttackPoint(Ecu1.passFirmwareValidation);
    attacker.attack();

    // Test that firmware is uploaded to ECU as expected
    Ecu1.changeOperationMode.assertCompromisedInstantaneously();
    Ecu1.uploadFirmware.assertCompromisedInstantaneously();
    // Test that uploading firmware leads to expected attack stepss
    vNet2.j1939Attacks.assertCompromisedInstantaneously();
    vNet2.messageInjection.assertCompromisedInstantaneously();
    dataflow.request.assertCompromisedInstantaneously();
    dataflow.respond.assertCompromisedWithEffort();
  }

  @AfterEach
  public void deleteModel() {
    Asset.allAssets.clear();
    AttackStep.allAttackSteps.clear();
    Defense.allDefenses.clear();
  }
}
