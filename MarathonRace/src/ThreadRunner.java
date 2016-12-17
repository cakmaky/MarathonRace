
public class ThreadRunner extends Thread {
		private String name;
		private int speed;
		private int restRatio;
		private int complatedDistance;
		private final static int TOTAL_DISTANCE = 500;

	/**
	 * Creates a ThreadRunner object and initializes the complatedDistance to 0 
	 * @param name - name of the thread
	 * @param speed - speed of the thread
	 * @param restRatio - Ratio of rest or run for a thread 
	 */
		public ThreadRunner(String name,int speed, int restRatio ){
		this.name = name;
		this.speed = speed;
		this.restRatio = restRatio;
		complatedDistance = 0;
	}
	/**
	 * Consists of a loop that repeats until one of the runners has reached
	 * 1000 meters.Each time through the loop, the thread should decide whether
	 * it should run or rest based on a random number and the percentage 
	 * The run method should sleep for 100 milliseconds on each repetition
	 * of the loop.  
	 */
		
	@Override 
	public void run(){
		Thread ct = Thread.currentThread();
		ct.setName(name); 
		while(complatedDistance < TOTAL_DISTANCE && !ct.isInterrupted()){
			int rand = getRandomNumber();
			if(ct.isInterrupted()){
				break;
			}
			else if(rand > restRatio){
				synchronized (getClass()) {
					complatedDistance += speed;
					if(!ct.isInterrupted()){
						System.out.println(ct.getName() + " : " +complatedDistance); 	
						if(complatedDistance >= TOTAL_DISTANCE){
							System.out.println(ct.getName() + " : I finished"); 	
							RaceApp.finished(ct);
						}
					}
				}
			}
			try{
				Thread.sleep(100); 	
			} catch(InterruptedException e) {
				ct.interrupt();
				break;
			}		
		}	
	}
	/**
	 * Returns the random number between 1 and 100
	 * @return random number between 1 and 100
	 */
	public int getRandomNumber(){
		return (int)(Math.random()*100);
	}
}
