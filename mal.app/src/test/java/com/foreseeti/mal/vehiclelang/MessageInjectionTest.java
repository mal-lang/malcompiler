package com.foreseeti.mal.vehiclelang;

import static com.foreseeti.mal.TestUtils.clearTestSystem;
import static com.foreseeti.mal.TestUtils.initTestSystem;

import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vehicle.Account;
import vehicle.ConnectionlessDataflow;
import vehicle.ECU;
import vehicle.Firmware;
import vehicle.TransmitterService;
import vehicle.VehicleNetwork;
import vehicle.Vulnerability;

public class MessageInjectionTest {

  @BeforeAll
  public static void init() {
    initTestSystem();
  }

  @AfterAll
  public static void exit() {
    clearTestSystem();
  }

  @Test
  public void testNetworkMessageInjection() {
    // Testing simple message injection after physical access.
    /*
        Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
                      |
                   Dataflow
    */
    // TARGET: vNet1.messageInjection ENTRY_POINT: vNet1.physicalAccess

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 = new ECU("Ecu#1");
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    vNet1.addDataflows(dataflow);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(vNet1.physicalAccess);
    attacker.attack();

    vNet1.accessNetworkLayer.assertCompromisedInstantaneously();
    vNet1.messageInjection.assertCompromisedInstantaneously();
    vNet1.eavesdrop.assertCompromisedInstantaneously();
    vNet1.denialOfService.assertCompromisedInstantaneously();

    vNet2.messageInjection.assertUncompromised();

    dataflow.denialOfService.assertCompromisedInstantaneously();
    dataflow.eavesdrop.assertCompromisedInstantaneously();

    dataflow.maliciousTransmitBypassConflitionProtection.assertCompromisedWithEffort();
    dataflow.transmit.assertCompromisedWithEffort();
  }

  @Test
  public void testServicekMessageInjectionConflictProtect() {
    // Testing message injection from network when confliction protection is disabled.
    /*
        Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
          |            ||
          |            |------ Dataflow#2
    Transmitter <---> Dataflow
    */
    // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: vNet1.physicalAccess

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU(
            "Ecu#1", true,
            false); // Enabled operation mode and DISABLED message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#2");
    TransmitterService service = new TransmitterService("Transmitter");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(vNet1.physicalAccess);
    attacker.attack();

    vNet1.messageInjection.assertCompromisedInstantaneously();
    vNet2.messageInjection.assertUncompromised();

    Ecu1.bypassMessageConfliction.assertUncompromised();
    Ecu1._networkServiceMessageInjection.assertCompromisedInstantaneously();

    service.serviceMessageInjection.assertCompromisedInstantaneously();
    dataflow.maliciousTransmit.assertCompromisedInstantaneously();
    dataflow.transmit.assertCompromisedInstantaneously();
    dataflow2.transmit.assertCompromisedWithEffort();
  }

  @Test
  public void testServicekMessageInjectionNoConflictProtect() {
    // Testing service message injection from network when confliction protection is enabled.
    /*
        Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
          |            ||
          |            |------ Dataflow#2
    Transmitter <---> Dataflow
    */
    // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: vNet1.physicalAccess

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU("Ecu#1", true, true); // Enabled operation mode and message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow#3");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#4");
    TransmitterService service = new TransmitterService("Transmitter");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(vNet1.physicalAccess);
    attacker.attack();

    vNet1.messageInjection.assertCompromisedInstantaneously();
    vNet2.messageInjection.assertUncompromised();

    Ecu1.bypassMessageConfliction.assertUncompromised();
    service.serviceMessageInjection.assertUncompromised();

    dataflow.transmit.assertCompromisedWithEffort();
    dataflow2.transmit.assertCompromisedWithEffort();
  }

  @Test
  public void testServicekMessageInjectionFromECU() {
    // Testing message injection from connected Ecu when confliction protection is disabled.
    /*
        Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
          |            ||
          |            |------ Dataflow#2
    Transmitter <---> Dataflow
    */
    // TARGET: dataflow & datafaflow2.transmit BUT uncompromised ENTRY_POINT: Ecu1.connect

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU(
            "Ecu#1", true,
            false); // Enabled operation mode and disabled message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#2");
    TransmitterService service = new TransmitterService("Transmitter");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(Ecu1.connect);
    attacker.attack();

    // vNet1.messageInjection.assertCompromisedInstantaneously();
    // vNet2.messageInjection.assertUncompromised();

    // Ecu1.bypassMessageConfliction.assertUncompromised();
    Ecu1.authenticate.assertUncompromised();
    service.serviceMessageInjection.assertUncompromised();

    dataflow.maliciousTransmit.assertUncompromised();
    dataflow.transmit.assertUncompromised();

    // dataflow2.transmit.assertCompromisedWithEffort();
  }

  @Test
  public void testNetworkMessageInjectionAfterVuln() {
    // Testing network message injection after exploiting vulnerability.
    /*
      Vulnerability(A)           Dataflow#2
            |                        |
         Account <---> Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
            |            |           |
            |---> Transmitter <--> Dataflow
    */
    // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: vuln.exploit

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU("Ecu#1", true, true); // Enabled operation mode and message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow#5");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#6");
    TransmitterService service = new TransmitterService("Service");

    Account account = new Account("Root User");
    Vulnerability vuln = new Vulnerability("Vulnerability");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu1.addAccount(account);
    Ecu1.addConnectionVulnerabilities(vuln);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    vuln.addPrivileges(account);
    service.addAccount(account);
    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(vuln.exploit);
    attacker.addAttackPoint(Ecu1.connect);
    attacker.attack();

    Ecu1.authenticate.assertCompromisedInstantaneously();
    Ecu1.access.assertCompromisedInstantaneouslyFrom(account.compromise);
    Ecu1.bypassMessageConfliction.assertCompromisedInstantaneously();

    service.access.assertCompromisedInstantaneously();
    service.serviceMessageInjection.assertCompromisedInstantaneously();
    Ecu2.connect.assertCompromisedInstantaneously();

    vNet1.messageInjection.assertUncompromised();
    vNet2.messageInjection.assertUncompromised();

    dataflow.transmit.assertCompromisedInstantaneously();

    dataflow2.transmit.assertUncompromised();
  }

  @Test
  public void testNetworkMessageInjectionAfterConnectionVuln() {
    // Testing network message injection after exploiting vulnerability.
    /*
      Vulnerability              Dataflow#2
            |                        |
         Account <---> Ecu#1 <---> vNet1 <---> Ecu#2(A)
            |            |           |
            |---> Transmitter <--> Dataflow
    */
    // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: Ecu#2.access

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU("Ecu#1", true, true); // Enabled operation mode and message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow#5");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#6");
    TransmitterService service = new TransmitterService("Service");

    Account account = new Account("Root User");
    Vulnerability vuln = new Vulnerability("Vulnerability");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu1.addAccount(account);
    Ecu1.addConnectionVulnerabilities(vuln);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    vuln.addPrivileges(account);
    service.addAccount(account);
    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(Ecu2.access);
    attacker.attack();

    Ecu1.connect.assertCompromisedInstantaneously();
    Ecu1.authenticate.assertCompromisedWithEffort();
    Ecu1.access.assertCompromisedWithEffort();
    Ecu1.bypassMessageConfliction.assertCompromisedWithEffort();

    service.access.assertCompromisedWithEffort();
    service.serviceMessageInjection.assertCompromisedWithEffort();

    vNet1.access.assertCompromisedInstantaneously();
    vNet1.messageInjection.assertUncompromised();
    vNet2.messageInjection.assertUncompromised();

    dataflow.transmit.assertCompromisedWithEffort();

    dataflow2.transmit.assertUncompromised();
  }

  @Test
  public void testNetworkMessageInjectionAfterFirmwareUpload() {
    // Testing network message injection after directly uploading custom firmware on ECU.
    /*
                      Dataflow#2
                          |
            Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
              |           |
       Transmitter <--> Dataflow
    */
    // TARGET: dataflow & datafaflow2.transmit ENTRY_POINT: Ecu#1.maliciousFirmwareUpload

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU("Ecu#1", true, true); // Enabled operation mode and message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow#7");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#8");
    TransmitterService service = new TransmitterService("Service");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(Ecu1.maliciousFirmwareUpload);
    attacker.attack();

    Ecu1.access.assertCompromisedInstantaneously();
    Ecu1.bypassMessageConfliction.assertCompromisedInstantaneously();

    vNet1.messageInjection.assertCompromisedInstantaneously();
    vNet2.messageInjection.assertUncompromised();

    dataflow.transmit.assertCompromisedInstantaneously();
    dataflow2.maliciousTransmitBypassConflitionProtection.assertCompromisedWithEffort();
    dataflow2.transmit.assertCompromisedWithEffort();
  }

  @Test
  public void testProtectedNetworkMessageInjection() {
    // Testing network message injection on a protected network.
    /*
                                 Dataflow#2
                                     |
         Account <---> Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
            |            |           |
            |---> Transmitter <--> Dataflow
    */
    // TARGET: dataflow & datafaflow2.tranmsit ENTRY_POINT: Ecu#1.connect & Service.connect

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU("Ecu#1", true, true); // Enabled operation mode and message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow#9");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#10");
    TransmitterService service = new TransmitterService("Transmitter");

    Account account = new Account("Root User");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu1.addAccount(account);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    service.addAccount(account);
    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(Ecu1.connect);
    attacker.addAttackPoint(service.connect);
    attacker.attack();

    Ecu1.access.assertUncompromised();

    vNet1.messageInjection.assertUncompromised();
    vNet2.messageInjection.assertUncompromised();

    dataflow.transmit.assertUncompromised();
    dataflow2.transmit.assertUncompromised();
  }

  @Test
  public void testSeeminglyProtectedNetworkMessageInjection() {
    // Testing network message injection on a protected network.
    /*
                     Firmware    Dataflow#2
                         |           |
         Account <---> Ecu#1 <---> vNet1 <---> Ecu#2 <---> vNet2
            |            |           |
            |---> Transmitter <--> Dataflow
    */
    // TARGET: dataflow & datafaflow2.tranmsit ENTRY_POINT: Ecu#1.connect & Service.connect

    System.out.println(
        "### "
            + Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()); // Printing the test's name

    ECU Ecu1 =
        new ECU("Ecu#1", true, true); // Enabled operation mode and message confliction protection.
    ECU Ecu2 = new ECU("Ecu#2");
    Firmware firmware = new Firmware("Firmware", true, false);
    ConnectionlessDataflow dataflow = new ConnectionlessDataflow("Dataflow#9");
    ConnectionlessDataflow dataflow2 = new ConnectionlessDataflow("Dataflow#10");
    TransmitterService service = new TransmitterService("Transmitter");

    Account account = new Account("Root User");

    VehicleNetwork vNet1 = new VehicleNetwork("vNet1");
    VehicleNetwork vNet2 = new VehicleNetwork("vNet2");

    Ecu1.addVehiclenetworks(vNet1);
    Ecu1.addExecutees(service);
    Ecu1.addAccount(account);
    Ecu1.addFirmware(firmware);
    Ecu2.addVehiclenetworks(vNet1);
    Ecu2.addVehiclenetworks(vNet2);

    service.addAccount(account);
    service.addDataflows(dataflow);
    vNet1.addDataflows(dataflow);
    vNet1.addDataflows(dataflow2);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(Ecu1.connect);
    attacker.addAttackPoint(service.connect);
    attacker.attack();

    vNet1.messageInjection.assertUncompromised();
    vNet1.messageInjection.assertUncompromisedFrom(Ecu1.uploadFirmware);
    vNet2.messageInjection.assertUncompromised();

    dataflow.transmit.assertUncompromised();
    dataflow2.transmit.assertUncompromised();
  }

  @AfterEach
  public void deleteModel() {
    Asset.allAssets.clear();
    AttackStep.allAttackSteps.clear();
    Defense.allDefenses.clear();
  }
}
