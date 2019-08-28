/*
 * Copyright 2019 Foreseeti AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mal_lang.compiler.test.bled;

import bled.Firewall;
import bled.Host;
import bled.IP;
import bled.Network;
import core.Asset;
import core.AttackStep;
import core.Attacker;
import core.Defense;
import java.net.URL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.test.MalTest;

public class TestFirewall extends MalTest {

  @Test
  public void testNetwork() {
    // Hosts may access other hosts on the same networks that are explicitly allowed by the
    // firewall(s). Attacker has access to VENI.
    //                     +---------+
    //                     | NETWORK |
    //        +------------+  luto   +-----------+
    //        |            +---------+           |
    //        |                |                 |
    //    blocked              |                 |
    //        |                |                 |
    //    +------+  allow   +------+  allow   +------+
    //    | terg <----------+ veni +--------->+ ursu |
    //    +------+          +------+          +------+
    //                                           |
    //                     +---------+           |
    //                     | NETWORK |           |
    //        +------------+  eram   +-----------+
    //        |            +---------+           |
    //        |                |                 |
    //    blocked              |                 |
    //        |                |                 |
    //    +------+          +------+   allow  +--v---+
    //    | ambe <-------+  | bele <----------+ alla |
    //    +------+       |  +------+  |       +------+
    //                   |            |
    //                   +------------+

    Network network_luto = new Network("luto");
    Network network_eram = new Network("eram");

    // LUTO ONLY
    Host host_veni = new Host("veni");
    host_veni.addIp(new IP("veni::127.0.0.1"));
    network_luto.addHosts(host_veni);

    Host host_terg = new Host("terg");
    host_terg.addIp(new IP("terg::127.0.0.1"));
    network_luto.addHosts(host_terg);

    // ERAM ONLY
    Host host_alla = new Host("alla");
    host_alla.addIp(new IP("alla::127.0.0.1"));
    network_eram.addHosts(host_alla);

    Host host_ambe = new Host("ambe");
    host_ambe.addIp(new IP("ambe::127.0.0.1"));
    network_eram.addHosts(host_ambe);

    Host host_bele = new Host("bele");
    host_bele.addIp(new IP("bele::127.0.0.1"));
    network_eram.addHosts(host_bele);

    // BOTH
    Host host_ursu = new Host("ursu");
    host_ursu.addIp(new IP("ursu::127.0.0.1"));
    network_luto.addHosts(host_ursu);
    network_eram.addHosts(host_ursu);

    // firewall for all hosts at LUTO
    Firewall fw_luto = new Firewall("fw_luto");
    fw_luto.addBlockedIPs(host_terg.ip);
    for (Host host : network_luto.hosts) {
      host.addFws(fw_luto);
    }

    // firewall for all hosts at ERAM
    Firewall fw_eram = new Firewall("fw_eram");
    fw_eram.addBlockedIPs(host_ambe.ip);
    for (Host host : network_eram.hosts) {
      host.addFws(fw_eram);
    }

    Firewall fw_veni_personal = new Firewall("fw_veni_personal");
    fw_veni_personal.addAllowedIPs(host_ursu.ip);
    fw_veni_personal.addAllowedIPs(host_terg.ip);
    host_veni.addFws(fw_veni_personal);

    Firewall fw_ursu_personal = new Firewall("fw_ursu_personal");
    fw_ursu_personal.addAllowedIPs(host_alla.ip);
    host_ursu.addFws(fw_ursu_personal);

    Firewall fw_alla_personal = new Firewall("fw_alla_personal");
    fw_alla_personal.addAllowedIPs(host_bele.ip);
    fw_alla_personal.addAllowedIPs(host_ambe.ip);
    host_alla.addFws(fw_alla_personal);

    Attacker attacker = new Attacker();
    attacker.addAttackPoint(host_veni.access);
    System.out.println("ATTACKER");

    URL resource = TestFirewall.class.getResource("/bled/attackerProfile.ttc");
    attacker.attack(resource.getPath());

    // LUTO
    host_ursu.access.assertCompromisedInstantaneously();
    host_terg.access.assertUncompromised();
    // ERAM
    host_alla.access.assertCompromisedInstantaneously();
    host_ambe.access.assertUncompromised();
    host_bele.access.assertCompromisedInstantaneously();
  }

  @AfterEach
  public void clearModel() {
    Asset.allAssets.clear();
    AttackStep.allAttackSteps.clear();
    Defense.allDefenses.clear();
  }
}
