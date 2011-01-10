package net.java.dev.moskito.core.treshold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import net.java.dev.moskito.core.producers.IStats;


public class Threshold implements IntervalUpdateable{
	
	private static Logger log = Logger.getLogger(Threshold.class);
	private static AtomicInteger instanceCounter = new AtomicInteger(0);
	
	private ThresholdStatus status;
	private ThresholdDefinition definition;
	private List<ThresholdConditionGuard> guards;

	private IStats stats;
	private String lastValue;
	private String statusChange = null;
	private long statusChangeTimestamp;
	
	private int instanceNumber;
	
	public Threshold(ThresholdDefinition aDefinition){
		definition = aDefinition;
		status = ThresholdStatus.OFF;
		lastValue = "none yet";
		guards = new ArrayList<ThresholdConditionGuard>();
		instanceNumber = instanceCounter.incrementAndGet();
	}
	
	public void tieToStats(IStats aStatsObject){
		stats = aStatsObject;
	}
	
	public void addGuard(ThresholdConditionGuard guard){
		guards.add(guard);
	}
	
	public List<ThresholdConditionGuard> getGuards(){
		ArrayList<ThresholdConditionGuard> ret = new ArrayList<ThresholdConditionGuard>(guards.size());
		ret.addAll(guards);
		return ret;
	}

	public ThresholdStatus getStatus() {
		return status;
	}

	public ThresholdDefinition getDefinition() {
		return definition;
	}

	public IStats getStats() {
		return stats;
	}

	public String getLastValue() {
		return lastValue;
	}
	
	public void update(){
		if (!isActivated()){
			return;
		}
		
		String previousValue = lastValue;
		lastValue = stats.getValueByNameAsString(definition.getValueName(), definition.getIntervalName(), definition.getTimeUnit());
		
		ThresholdStatus futureStatus = status == ThresholdStatus.OFF ? ThresholdStatus.OFF : ThresholdStatus.GREEN;
		for (ThresholdConditionGuard guard : guards){
			try{
				ThresholdStatus newStatus = guard.getNewStatusOnUpdate(previousValue, lastValue, status, this);
				if (newStatus.overrules(futureStatus)){
					futureStatus = newStatus;
				}
			}catch(Exception e){
				log.warn("Error in ThresholdConditionGuard: "+guard+" in getNewStatusOnUpdate("+previousValue+", "+lastValue+", "+status+", "+this, e);
			}
		}
		
		//generate alert.
		if (status != futureStatus){
			//generate alert
			statusChange = status+" --> "+futureStatus;
			statusChangeTimestamp = System.currentTimeMillis();
			AlertHistory.INSTANCE.addAlert(new ThresholdAlert(this, status, futureStatus, previousValue, lastValue));
		}
		status = futureStatus;
	}

	public String getName(){
		return getDefinition().getName();
	}
	 
	public boolean isActivated(){
		return stats != null;
	}
	
	@Override public String toString(){
		return getName()+" "+getStatus()+" Def: "+getDefinition()+" LastValue: "+getLastValue()+", Guards: "+guards+" active: "+isActivated()+", Stats: "+getStats();
	}

	public String getStatusChange() {
		return statusChange;
	}

	public void setStatusChange(String statusChange) {
		this.statusChange = statusChange;
	}

	public long getStatusChangeTimestamp() {
		return statusChangeTimestamp;
	}

	public void setStatusChangeTimestamp(long statusChangeTimestamp) {
		this.statusChangeTimestamp = statusChangeTimestamp;
	}
	
	public int getInstanceNumber(){
		return instanceNumber;
	}
}