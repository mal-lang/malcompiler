package com.foreseeti.mal.vehiclelang;

import com.foreseeti.mal.test.MalTest;
import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import vehicle.CANNetwork;
import vehicle.ConnectionlessDataflow;
import vehicle.ECU;
import vehicle.FlexRayNetwork;
import vehicle.GatewayECU;
import vehicle.LINNetwork;
import vehicle.TransmitterService;
import vehicle.VehicleNetwork;

public class CoreVehicleNetworkTest extends MalTest {

  @Test // To make it work I added forwarding on GatewayECU
  public void testGatewayECUAccess() {
    // Testing gateway ECU access.
    GatewayECU gECU = new GatewayECU("GwECU");

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(gECU.connect);
    attacker.addAttackPoint(gECU.authenticate);

    attacker.attack();

    gECU.authenticatedAccess.assertCompromisedInstantaneously();
    gECU.access.assertCompromisedInstantaneously();
    gECU.denialOfService.assertCompromisedInstantaneously();
    gECU.forwarding.assertCompromisedInstantaneously();
  }

  @Test
  public void simpleServiceMessageInjection() {
    /*
      Transmitter <---> Dataflow
    */
    // TARGET: dataflow.transmit ENTRY_POINT: Service.access

    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
    TransmitterService service = new TransmitterService("pwnedTransmitter");

    service.addDataflows(dataflow);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(service.access);
    attacker.attack();

    service.serviceMessageInjection.assertUncompromised();
    dataflow.transmit.assertCompromisedInstantaneously();
  }

  @Test
  public void testMitmNetwork() {
    // Testing MitM attack on dataflow and network.
    /*
         Service <---> Dataflow
                           |
                        Network
    */
    // Entry point: network.manInTheMiddle
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
    TransmitterService service = new TransmitterService("Transmitter");
    VehicleNetwork network = new VehicleNetwork("Network");

    service.addDataflows(dataflow);
    network.addDataflows(dataflow);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(network.manInTheMiddle);

    attacker.attack();

    dataflow.manInTheMiddle.assertCompromisedInstantaneously();
    dataflow.transmit.assertCompromisedInstantaneously();
    service.connect.assertCompromisedInstantaneously();
  }

  @Test
  public void testPhysicalAccess() {
    // Testing MitM attack on dataflow and network.
    /*
          Network <---> Dataflow
    */
    // Entry point: network.physicalAccess
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
    VehicleNetwork network = new VehicleNetwork("Network");

    network.addDataflows(dataflow);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(network.physicalAccess);

    attacker.attack();

    network.eavesdrop.assertCompromisedInstantaneously();
    network.denialOfService.assertCompromisedInstantaneously();

    dataflow.denialOfService.assertCompromisedInstantaneously();
    dataflow.eavesdrop.assertCompromisedInstantaneously();

    dataflow.transmit.assertCompromisedWithEffort();
  }

  @Test
  public void testCANNetworkSpecificAttacks() {
    // Testing CAN network specific attacks.
    /*
        Ecu#1 <---> CAN
    */
    // TARGET: CAN attacks ENTRY_POINT: CAN.physicalAccess
    CANNetwork canNet = new CANNetwork("CANNetwork", false);
    ECU ecu = new ECU("ECU", false, false);

    canNet.addNetworkECUs(ecu);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(canNet.physicalAccess);
    attacker.attack();

    canNet.exploitArbitration.assertCompromisedWithEffort();
    canNet.busOffAttack.assertCompromisedWithEffort();
    ecu.offline.assertCompromisedWithEffort();
  }

  @Test
  public void testFlexNetworkSpecificAttacks() {
    // Testing FlexRay network specific attacks.
    FlexRayNetwork flexNet = new FlexRayNetwork("FlexNetwork", false);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(flexNet.physicalAccess);
    attacker.attack();

    flexNet.commonTimeBaseAttack.assertCompromisedWithEffort();
    flexNet.exploitBusGuardian.assertCompromisedWithEffort();
    flexNet.sleepFrameAttack.assertCompromisedWithEffort();
  }

  @Test
  public void testLINNetworkSpecificAttacks() {
    // Testing LIN network specific attacks.
    LINNetwork linNet = new LINNetwork("LINNetwork", false);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(linNet.physicalAccess);
    attacker.attack();

    linNet.injectHeaderOrTimedResponse.assertCompromisedWithEffort();
    linNet.injectBogusSyncBytes.assertCompromisedWithEffort();
    linNet.gainLINAccessFromCAN.assertUncompromised();
  }

  @AfterEach
  public void deleteModel() {
    Asset.allAssets.clear();
    AttackStep.allAttackSteps.clear();
    Defense.allDefenses.clear();
  }
}
