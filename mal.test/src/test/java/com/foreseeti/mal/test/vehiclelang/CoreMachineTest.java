package com.foreseeti.mal.test.vehiclelang;

import com.foreseeti.mal.test.MalTest;
import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import vehicle.Account;
import vehicle.Data;
import vehicle.Machine;
import vehicle.Software;

public class CoreMachineTest extends MalTest {

  @Test
  public void testMachineAccess() {
    // Testing proper access to a machine.
    Machine machine = new Machine();

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(machine.connect);
    attacker.addAttackPoint(machine.authenticate);

    attacker.attack();

    machine.access.assertCompromisedInstantaneously();
    machine.denialOfService.assertCompromisedInstantaneously();
  }

  @Test
  public void testBypassMachineAccess() {
    // Testing bypass access control to a machine.
    Machine machine = new Machine();

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(machine.bypassAccessControl);

    attacker.attack();

    machine.access.assertCompromisedInstantaneously();
    machine.denialOfService.assertCompromisedInstantaneously();
  }

  @Test
  public void testSoftwareHostToGuest() {
    // Testing compromise account on a machine.
    /*
    Account <---> Machine <---> Software2
       |             |
    Software1 <------
    */
    // TARGET: softwares ENTRY_POINT: account.compromise and machine.connect
    Machine machine = new Machine("Machine");
    Software software1 = new Software("Software1");
    Software software2 = new Software("Software2");
    Account account = new Account("Account");

    machine.addAccount(account);
    software1.addExecutor(machine);
    software2.addExecutor(machine);
    software1.addAccount(account);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(machine.connect);
    attacker.addAttackPoint(account.compromise);

    attacker.attack();

    machine.access.assertCompromisedInstantaneously();
    software1.connect.assertCompromisedInstantaneously();
    software1.access.assertCompromisedInstantaneously();
    software2.connect.assertCompromisedInstantaneously();
    software2.access.assertUncompromised();
  }

  @Test
  public void testSoftwareGuestToHost() {
    // Testing machine access from software.
    Machine machine = new Machine("Machine12");
    Software software = new Software("Software123");

    software.addExecutor(machine);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(software.connect);
    attacker.addAttackPoint(software.authenticate);

    attacker.attack();

    software.access.assertCompromisedInstantaneously();
    machine.connect.assertCompromisedInstantaneously();
    machine.access.assertUncompromised();
  }

  @Test
  public void testMachineAccountDataRWD() {
    // Testing data read access from account compromise.
    /*
    Account <---> Machine
       |             |
     Data(read) <----
    */
    // TARGET: Data.read ENTRY_POINT: account.compromise and machine.connect
    Machine machine = new Machine("Machine");
    Account account = new Account("Account");
    Data data = new Data("Data");

    machine.addAccount(account);
    machine.addData(data);
    account.addReadData(data);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(machine.connect);
    attacker.addAttackPoint(account.compromise);

    attacker.attack();

    data.requestAccess.assertCompromisedInstantaneously();
    data.anyAccountRead.assertCompromisedInstantaneously();
    data.read.assertCompromisedInstantaneously();
    data.anyAccountWrite.assertUncompromised();
    data.write.assertUncompromised();
    data.anyAccountDelete.assertUncompromised();
    data.delete.assertUncompromised();

    machine.authenticate.assertCompromisedInstantaneously();
  }

  @AfterEach
  public void deleteModel() {
    Asset.allAssets.clear();
    AttackStep.allAttackSteps.clear();
    Defense.allDefenses.clear();
  }
}
