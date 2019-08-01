package com.foreseeti.mal.vehiclelang;

import com.foreseeti.mal.test.MalTest;
import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import vehicle.ECU;

public class CoreEcuTest extends MalTest {

  @Test
  public void testConnectEcuAttacks() {
    // Testing ECU attacks on connect with all defenses enabled.
    ECU ecu =
        new ECU("ECU", true, true); // Enabled operation mode and message confliction protection.

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(ecu.connect);
    attacker.attack();

    ecu.changeOperationMode.assertUncompromised();
    ecu.access.assertUncompromised();
    ecu.gainLINAccessFromCAN.assertUncompromised();
  }

  @Test
  public void testConnectEcuAttacks2() {
    // Testing ECU attacks on connect with some defenses enabled.
    ECU ecu = new ECU("ECU2", false, true); // Enabled only message confliction protection.

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(ecu.connect);
    attacker.attack();

    ecu.attemptChangeOperationMode.assertCompromisedWithEffort();
    ecu.changeOperationMode.assertUncompromised();
    ecu.access.assertUncompromised();
    ecu.gainLINAccessFromCAN.assertUncompromised();
  }

  @Test
  public void testAccessEcuAttacks() {
    // Testing ECU attacks on access with all defenses enabled.
    ECU ecu =
        new ECU("ECU3", true, true); // Enabled operation mode and message confliction protection.

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(ecu.access);
    attacker.attack();

    ecu.changeOperationMode.assertUncompromised();
    ecu.gainLINAccessFromCAN.assertCompromisedInstantaneously();
  }

  @Test
  public void testAccessEcuAttacks2() {
    // Testing ECU attacks on access with some defenses enabled.
    ECU ecu = new ECU("ECU4", false, true); // Enabled only message confliction protection.

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(ecu.access);
    attacker.attack();

    ecu.changeOperationMode.assertCompromisedInstantaneously();
    ecu.attemptChangeOperationMode.assertUncompromised();
    ecu.bypassMessageConfliction.assertCompromisedInstantaneously();
    ecu.gainLINAccessFromCAN.assertCompromisedInstantaneously();
  }

  @AfterEach
  public void deleteModel() {
    Asset.allAssets.clear();
    AttackStep.allAttackSteps.clear();
    Defense.allDefenses.clear();
  }
}
