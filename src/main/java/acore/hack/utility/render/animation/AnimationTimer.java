package acore.hack.utility.render.animation.advanced;

public class AnimationTimer {
   private long startTime;

   public AnimationTimer() {
      this.startTime = System.currentTimeMillis();
   }

   public boolean finished(int duration) {
      return this.getElapsedTime() >= duration;
   }

   public double getElapsedTime() {
      return System.currentTimeMillis() - this.startTime;
   }

   public void setMillis(long millis) {
      this.startTime = millis;
   }

   public void reset() {
      this.startTime = System.currentTimeMillis();
   }
}
