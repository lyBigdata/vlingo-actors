// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;

public class TestkitTest extends ActorsTest {
  
  @Test
  public void testTesterWorldPing() throws Exception {
    final TestActor<PingCounter> pingCounter =
            testWorld.actorFor(
                    PingCounter.class,
                    Definition.has(PingCounterActor.class, Definition.NoParameters));
    
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    
    assertEquals(3, TestWorld.Instance.get().allMessagesFor(pingCounter.address()).size());
    
    assertEquals(3, (int) pingCounter.viewTestState().valueOf("count"));
  }

  @Test
  public void testTesterPingPong() throws Exception {
    final TestActor<PongCounter> pongCounter =
            testWorld.actorFor(
                    PongCounter.class,
                    Definition.has(PongCounterActor.class, Definition.NoParameters));
    
    final TestActor<PingCounter> pingCounter =
            testWorld.actorFor(
                    PingCounter.class,
                    Definition.has(PingPongCounterActor.class, Definition.parameters(pongCounter.actor())));
    
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    
    assertEquals(3, TestWorld.Instance.get().allMessagesFor(pingCounter.address()).size());
    
    assertEquals(3, (int) pingCounter.viewTestState().valueOf("count"));
    
    assertEquals(3, (int) pongCounter.viewTestState().valueOf("count"));
  }

  @Test
  public void testThatUntilCompletesTimesOut() {
    final TestUntil until = TestUntil.happenings(1);
    assertFalse(until.completesWithin(100));
  }

  @Test
  public void testThatUntilCompletesWithinTimeOut() {
    final TestUntil until = TestUntil.happenings(1);

    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(50);
          until.happened();
        } catch (Exception e) {
          // ignore
        }
      }
    }.start();

    assertTrue(until.completesWithin(500));
  }

  public static interface PingCounter {
    void ping();
  }

  public static class PingCounterActor extends Actor implements PingCounter {
    private int count;
    
    public PingCounterActor() { }
    
    @Override
    public void ping() {
      ++count;
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("count", count);
    }
  }

  public static class PingPongCounterActor extends Actor implements PingCounter {
    private int count;
    
    private final PongCounter pongCounter;
    
    public PingPongCounterActor(final PongCounter pongCounter) {
      this.pongCounter = pongCounter;
    }
    
    @Override
    public void ping() {
      ++count;
      
      if (pongCounter != null) {
        pongCounter.pong();
      }
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("count", count);
    }
  }

  public static interface PongCounter {
    void pong();
  }

  public static class PongCounterActor extends Actor implements PongCounter {
    private int count;
    
    @Override
    public void pong() {
      ++count;
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("count", count);
    }
  }
}
