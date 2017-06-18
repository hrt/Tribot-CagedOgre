package scripts;

import java.awt.Color;
import java.awt.Graphics;

import org.tribot.api.DynamicClicking;
import org.tribot.script.*;
import org.tribot.script.interfaces.*;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;

import java.util.Random;

import java.util.Arrays;

// To start: setup cannon and stand on top of it

@ScriptManifest(authors = {"sandniga"}, category = "Private", name = "CagedOgre")
public class CagedOgre extends Script implements Painting {

  private Random rand = new Random();

  private final int[] RANGE_POTION = {2444, 169, 171, 173};

  private final int CANNONBALL = 2;

	private final int CANNON_BASE = 6;
	private final int CAGED_OGRE = 1153;

	private final RSTile[] LOAD_POSITION = {new RSTile(2526, 3371, 0)
                                        , new RSTile(2526, 3370, 0)
                                        , new RSTile(2529, 3370, 0)
										                    , new RSTile(2528, 3369, 0)};

  private RSTile CANNON_POSITION;

  private final int BROKEN_CANNON = 14916;

  private boolean onStart() {
    println("CagedOgre has started!");
    RSObject cannon = findNearest(6, CANNON_BASE);
    if (cannon != null && cannon.isOnScreen()) {
      CANNON_POSITION = cannon.getPosition();
    } else {
      return false;
    }
    return true;
  }  

  @Override
  public void run() {
    if (onStart()) {
      while (hasAmmo()) {
        sleep(loop());
      }
    }
    Login.logout();
  }


  public RSNPC findNearest (int id) {
    RSNPC[] npcs = NPCs.findNearest(id);

    for (RSNPC npc : npcs) {
      if (npc != null && !npc.isInCombat())
        return npc;
    }
    return null;
  }

  public RSObject findNearest (int d, int id) {
    RSObject[] objects = Objects.findNearest(d, id);

    for (RSObject object : objects) {
      if (object != null) {
        return object;
      }
    }
    return null;
  }

  private boolean isInSpot(RSTile pos) {
    return Arrays.asList(LOAD_POSITION).contains(pos);
  }

  private int ammoStack() {
    RSItem arrows = Equipment.getItem(Equipment.SLOTS.ARROW);
    return arrows.getStack();
  }

  private int cannonStack() {
    RSItem[] cannonballs = Inventory.find(CANNONBALL);
    for (RSItem cannonball : cannonballs) {
      if (cannonball != null) {
        return cannonball.getStack();
      }
    }
    return -1;
  }

  private boolean hasAmmo() {
    return ammoStack() > 1 && cannonStack() > 1;
  }

  private void moveToPosition(RSTile position) {
    if (!position.isOnScreen()) {
      Walking.blindWalkTo(position);
    }
    RSTile[] path = Walking.generateStraightScreenPath(position);
    Walking.walkScreenPath(path);
    Timer timer = new Timer(700 + rand.nextInt(130));
    timer.reset();
    while (timer.isRunning()) {
      sleep(100);
    }
    sleep(6000 + rand.nextInt(121));
  }

  private void moveToLoadPosition() {
    int index = rand.nextInt(3);
    RSTile position = LOAD_POSITION[index];
    moveToPosition(position);
  }

  private void waitUntilIdle(RSPlayer me) {
    Timer timer = new Timer(1100);
    timer.reset();
    while (timer.isRunning()) {
      sleep(100);
      if (me.getAnimation() != -1)
        timer.reset();
    }
  }

  private boolean isPotted() {
    int currentLevel = Skills.getCurrentLevel(Skills.SKILLS.RANGED);
    int actualLevel = Skills.getActualLevel(Skills.SKILLS.RANGED);
    return currentLevel > actualLevel;
  }

  private RSItem findPot() {
    RSItem[] pots = Inventory.find(RANGE_POTION);
    for (RSItem pot : pots) {
      if (pot != null){
        return pot;
      }
    }
    return null;
  }

  private void pot() {
    if (isPotted())
      return;
    RSItem pot = findPot();
    if (pot != null) {
      pot.click("Drink");
    }
  }

  private void loadCannon() {
    RSObject cannon = findNearest(6, CANNON_BASE);

    RSObject broken_cannon = findNearest(6, BROKEN_CANNON);

    int forgetToLoadChance = rand.nextInt(1000);

    if (broken_cannon != null
        && broken_cannon.isOnScreen()
        && broken_cannon.getPosition().equals(CANNON_POSITION)) {
      broken_cannon.click("Repair");
      sleep(500 + rand.nextInt(100));
    } else if (cannon != null
        && cannon.isOnScreen()
        && cannon.getPosition().equals(CANNON_POSITION) &&
        (forgetToLoadChance > 100 || Game.getSetting(1) == 0)) {
      cannon.click("Fire");
      sleep(500 + rand.nextInt(100));
    }
  }


  private int loop() {
    if (Login.getLoginState() != Login.STATE.INGAME) {
      Login.login();
    }

    RSPlayer me = Player.getRSPlayer();
    RSTile pos = Player.getPosition();

    if (!isInSpot(pos)) {
      moveToLoadPosition();
    } else {
      RSNPC caged_ogre = findNearest(CAGED_OGRE);
      if (caged_ogre != null && caged_ogre.isOnScreen()) {
        pot();
        caged_ogre.click("Attack");
        waitUntilIdle(me);
        loadCannon();
      }
    }
    return 43;
  }


  public void onPaint(Graphics g) {
    g.setColor(Color.GREEN);
    g.drawString("CagedOgre", 380, 330);
  }
}