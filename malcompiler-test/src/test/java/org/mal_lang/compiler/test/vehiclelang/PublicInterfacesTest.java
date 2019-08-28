package org.mal_lang.compiler.test.vehiclelang;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.test.MalTest;
import vehicle.AftermarketDongle;
import vehicle.CANNetwork;
import vehicle.ChargingPlugConnector;
import vehicle.ConnectionlessDataflow;
import vehicle.ECU;
import vehicle.OBD2Connector;
import vehicle.TransmitterService;

public class PublicInterfacesTest extends MalTest {

  @Test
  public void OBD2PhysicalAccessTest() {
    /*
    This test case models a simple OBD-II connector without defenses enabled.
    Checks that physical access to the port leads to the correct attack steps on the network

    OBD-II <---> CAN Bus <---> ECU#1
    */
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    // Start of test
    OBD2Connector obd2 = new OBD2Connector("obd2");
    CANNetwork can = new CANNetwork("CAN");

    obd2.addInterfacingNetworks(can);

    Attacker atk = new Attacker();
    atk.addAttackPoint(obd2.physicalAccess);
    atk.attack();

    can.accessNetworkLayer.assertCompromisedInstantaneously();
    can.eavesdrop.assertCompromisedInstantaneously();
    can.messageInjection.assertCompromisedInstantaneously();
  }

  @Test
  public void OBD2ConnectNoDefenseTest() {
    /*
      This test case models a simple OBD-II connector without defenses enabled.
      Checks that the _connectNoProtection step is taken when there is no physical protection defense
    */
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    // Start of test
    OBD2Connector obd2 = new OBD2Connector("obd2", false);

    Attacker atk = new Attacker();
    atk.addAttackPoint(obd2.connect);
    atk.attack();

    obd2._connectNoProtection.assertCompromisedInstantaneously();
    obd2.physicalAccess.assertCompromisedInstantaneouslyFrom(obd2._connectNoProtection);
  }

  @Test
  public void OBD2ConnectWithDefenseTest() {
    /*
      This test case models a simple OBD-II connector with defenses enabled.
      Checks that the _connectNoProtection step is *not* taken when there is a physical protection defense enabled
    */
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    // Start of test
    OBD2Connector obd2 = new OBD2Connector("obd2", true);

    Attacker atk = new Attacker();
    atk.addAttackPoint(obd2.connect);
    atk.attack();

    obd2._connectNoProtection.assertUncompromised();
    obd2.bypassConnectorProtection.assertCompromisedWithEffort();
    obd2.physicalAccess.assertCompromisedInstantaneouslyFrom(obd2.bypassConnectorProtection);
  }

  @Test
  public void ChargingPlugTest() {
    /*
    This test case models a real topology of an electric vehicle

    ChargingPlugConnector <---> CAN Bus <---> BMS <----
                                  |                   |
                              Dataflow <---> TransmitterService
    Naming:
    BMS = Battery Management System
    */
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    // Start of test
    ChargingPlugConnector chgPlug = new ChargingPlugConnector("chgPlug");
    CANNetwork can = new CANNetwork("CAN");
    ECU bms = new ECU("BMS");
    TransmitterService bmsService = new TransmitterService("BMS_Service");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("BatteryDataflow");

    chgPlug.addConnectedNetwork(can);
    bms.addVehiclenetworks(can);
    bms.addExecutees(bmsService);
    bmsService.addDataflows(dataflow);

    Attacker atk = new Attacker();
    atk.addAttackPoint(chgPlug.physicalAccess);
    atk.attack();

    can.accessNetworkLayer.assertCompromisedInstantaneously();
    can.eavesdrop.assertCompromisedInstantaneously();
    can.messageInjection.assertCompromisedInstantaneously();
    bms.connect.assertCompromisedInstantaneously();
    bmsService.connect.assertCompromisedInstantaneously();
    dataflow.transmit.assertCompromisedInstantaneously();
  }

  @Test
  public void AftermarketDongleTest() {
    /*
    This test case models an aftermarket dongle connected on the OBD-II port of a vehicle

    Dongle <---> OBD-II <---> CAN Bus

    */
    System.out.println("### " + Thread.currentThread().getStackTrace()[1].getMethodName());
    // Start of test
    boolean dongleIsHardened = true;
    AftermarketDongle dongle = new AftermarketDongle("dongle", dongleIsHardened);
    OBD2Connector obd2 = new OBD2Connector("obd2");
    CANNetwork can = new CANNetwork("CAN");

    dongle.addConnector(obd2);
    obd2.addInterfacingNetworks(can);

    Attacker atk = new Attacker();
    atk.addAttackPoint(dongle.connectDongle);
    atk.attack();

    if (dongleIsHardened == false) {
      dongle._connectToNetwork.assertCompromisedInstantaneously();
      can.accessNetworkLayer.assertCompromisedInstantaneously();
      can.eavesdrop.assertCompromisedInstantaneously();
      can.messageInjection.assertCompromisedInstantaneously();
    } else {
      dongle._connectToNetwork.assertUncompromised();
      can.accessNetworkLayer.assertUncompromised();
    }
  }

  @AfterEach
  public void deleteModel() {
    Asset.allAssets.clear();
    AttackStep.allAttackSteps.clear();
    Defense.allDefenses.clear();
  }
}
