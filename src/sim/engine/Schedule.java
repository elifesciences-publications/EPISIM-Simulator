package sim.engine;
import sim.util.*;

/**
   Schedule defines a scheduling queue in which events can be scheduled to occur
   at future time.  The time of the most recent event which has already occured
   is given by the <b>time()</b> method.  If the current time is <tt>BEFORE_SIMULATION</tt> (defined
   to be <tt>EPOCH - 1</tt>),
   then the schedule is set to the "time before time" (the schedule hasn't started running
   yet).  If the current time is <tt>AFTER_SIMULATION</tt> (positive infinity), then the schedule has run
   out of time.  <tt>EPOCH</tt> (0.0) is defined as the first timestep for which you can legally schedule a value.
   <tt>EPOCH_PLUS_ESPILON</tt> is defined as the smallest possible second timestep for which you can legally sechedule a value.
   If you're scheduling events to occur on integer timesteps, you may want to ensure that your simulation 
   does not run beyond <tt>MAXIMUM_INTEGER</tt> (9007199254740992L or 9.007199254740992E15).  For values of a 
   double d >= <tt>MAXIMUM_INTEGER</tt>, d + 1 == d !

   <p>An event is defined as a <b>Steppable</b> object. You can schedule events to either 
   occur a single time or to occur repeatedly at some interval.  If the event occurs repeatedly,
   the schedule will provide you with a <b>Stoppable</b> object on which you can call <b>stop()</b>
   to cancel all future repeats of the event.  If instead you wish to "stop" a single-time event from occuring
   before its time has come, you should do so through the use of a <b>TentativeStep</b> object.  At present
   you cannot delete objects from the Schedule -- just stop them and let them drop out in due course.

   <p>The schedule is pulsed by calling its <b>step(...)</b> method.  Each pulse, the schedule
   finds the minimum time at which events are scheduled, moves ahead to that time, and then calls
   all the events scheduled at that time.    Multiple events may be scheduled for the same time.
   No event may be scheduled for a time earlier than time().  If at time time() you schedule a new
   event for time time(), then actually this event will occur at time time()+epsilon, that is, the
   smallest possible slice of time greater than time().
   
   <p>Events at a step are further subdivided and scheduled according to their <i>ordering</i>.
   You specify the number of orderings in the constructor, and can't change them thereafter.
   If you specify N orderings, then the ordering values are 0 ... N-1.
   Objects for scheduled for lower orderings for a given time will be executed before objects with
   higher orderings for the same time.  If objects are scheduled for the same time and
   have the same ordering value, their execution will be randomly ordered with respect to one another.  At present,
   you can't use an event of a lower ordering to schedule events of the same time in a higher ordering
   even though those events haven't occurred yet.  Sorry.

   <p>Schedule is synchronized and threadsafe.  It's easy enough to de-synchronize the schedule by hand if you
   would like to.  In the worst case (schedule a single repeated steppable that does nothing), a synchronized
   schedule runs at 2/3 the speed of an unsynchronized schedule.  In a typical case (such as HeatBugs), 
   the difference between a synchronized and an unsynchronized schedule is less than 5% efficiency loss
   if it's visible at all.

   <p>You can get the number of times that step(...) has been called on the schedule by calling the getSteps() method.
   This value is incremented just as the Schedule exits its step(...) method and only if the method returned true.
   Additionally, you can get a string version of the current time with the getTimestamp(...) method.

   <p><b>Exception Handling</b>.  It's a common error to schedule a null event, or one with an invalid ordering or time.
   Schedule previously returned false or null in such situations, but this leaves the burden on the programmer to check,
   and programmers are forgetful!  We have changed Schedule to throw exceptions by default instead.  You can change
   Schedule back to returning false or null (perhaps if you want to handle the situations yourself more efficiently than
   catching an exception, or if you know what you're doing schedule-wise) by setting setThrowingScheduleExceptions(false).

*/
    

// "NEXT-STEP" FUNCTIONALITY
// If the user is always putting things in the very next step, the schedule has a facility
// for handling this special case.  However it appears that while this was somewhat helpful
// for the previous integer version, the overhead in the floating-point version of the
// Schedule is such that it doesn't buy anything.  So we have it commented out for now,
// to be simpler to understand, plus marks indicating where to uncomment if you want to play with it.


public class Schedule implements java.io.Serializable
    {
    public static final double EPOCH = 0.0;
    public static final double BEFORE_SIMULATION = EPOCH - 1.0;
    public static final double AFTER_SIMULATION = Double.POSITIVE_INFINITY;
    public static final double EPOCH_PLUS_EPSILON = Double.longBitsToDouble(Double.doubleToRawLongBits(EPOCH)+1L);
    public static final double MAXIMUM_INTEGER = 9.007199254740992E15;

    protected Heap[] queue;
    protected Steppable[][] substeps;
    protected int[] numSubsteps;
// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  protected Steppable[][] next;
  protected int[] numNext;
  protected double nextTime;
*/
    
    // the time
    protected double time;
    
    // the number of times step() has been called on m
    protected long steps;
    
    // whether or not the Schedule throws errors when it encounters an exceptional condition
    // on attempting to schedule an item
    protected boolean throwingScheduleExceptions = true;
    public synchronized void setThrowingScheduleExceptions(boolean val)
        {
        throwingScheduleExceptions = val;
        }
        
    public synchronized boolean isThrowingScheduleExceptions()
        {
        return throwingScheduleExceptions;
        }
    
    // resets the queues by replacing them, NOT reusing them.  This allows us to 
    // work properly in our schedule if the user resets the queues from within a Steppable;
    // see the comments in step()
    protected void resetQueues(final int numOrders)
        {
        queue = new Heap[numOrders];
        substeps = new Steppable[numOrders][11];
        numSubsteps = new int[numOrders];
// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  next = new Steppable[numOrders][11];
  numNext = new int[numOrders];
  nextTime = BEFORE_SIMULATION;
*/
        for(int x=0;x<queue.length;x++) queue[x] = new Heap();
        }
    
    public Schedule(final int numOrders)
        {
        resetQueues(numOrders);
        time = BEFORE_SIMULATION;
        steps = 0;
        }
    
    /** Creates a Schedule with a single order */
    public Schedule()
        {
        this(1);
        }
    
    public synchronized double time() { return time; }
    
    /** Returns the current time in string format. If the time is BEFORE_SIMULATION, then beforeSimulationString is
        returned.  If the time is AFTER_SIMULATION, then afterSimulationString is returned.  Otherwise a numerical
        representation of the time is returned. */
    public synchronized String getTimestamp(final String beforeSimulationString, final String afterSimulationString)
        {
        return getTimestamp(time(), beforeSimulationString, afterSimulationString);
        }
    
    /** Returns a given time in string format. If the time is BEFORE_SIMULATION, then beforeSimulationString is
        returned.  If the time is AFTER_SIMULATION, then afterSimulationString is returned.  Otherwise a numerical
        representation of the time is returned. */
    public String getTimestamp(double time, final String beforeSimulationString, final String afterSimulationString)
        {
        if (time <= BEFORE_SIMULATION) return beforeSimulationString;
        if (time >= AFTER_SIMULATION) return afterSimulationString;
        if (time == (long)time) return Long.toString((long)time);
        return Double.toString(time);
        }

    public synchronized long getSteps() { return steps; }

    // roughly doubles the array size, retaining the existing elements
    protected Steppable[] increaseSubsteps(final Steppable[] substeps)
        {
        return increaseSubsteps(substeps,substeps.length*2+1);
        }
        
    // increases substeps to n length -- which had better be bigger than substeps.length!
    protected Steppable[] increaseSubsteps(final Steppable[] substeps, final int n)
        {
        Steppable[] newsubstep = new Steppable[n];
        System.arraycopy(substeps,0,newsubstep,0,substeps.length);
        return newsubstep;
        }
    
    /** Empties out the schedule and resets it to a pristine state BEFORE_SIMULATION, with steps = 0.*/
    public synchronized void reset()
        {
        resetQueues(queue.length);        // make new queues
        time = BEFORE_SIMULATION;
        steps = 0;
        }
    
    /** Returns true if the schedule has nothing left to do. */
    public synchronized boolean scheduleComplete()
        {
        return _scheduleComplete();
        }
    
    protected boolean _scheduleComplete()
        {
// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  if (time < nextTime) return false;
*/
        for(int x=0;x<queue.length;x++)
            if (!queue[x].isEmpty())
                return false;
        return true;
        }
    
    /** Steps the schedule, gathering and ordering all the items to step on the next time step (skipping
        blank time steps), and then stepping all of them in the decided order.  
        Returns FALSE if nothing was stepped -- the schedule is exhausted or time has run out. */
    
    public synchronized boolean step(final SimState state)
        {
        final double AFTER_SIMULATION = this.AFTER_SIMULATION;  // a little faster
        
        if (time==AFTER_SIMULATION) 
            return false;
        
        double t = AFTER_SIMULATION;

        // store the queue substep information here even if the user deletes it
        // from within a step(), so we can continue forward anyway
        final int[] _numSubsteps = numSubsteps;
        final Steppable[][] _substeps = substeps;
        
        // these are locals for speed -- probably no big deal
        Heap[] queue = this.queue;
// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  int[] numNext = this.numNext;
  double nextTime = this.nextTime;
*/

        if (!_scheduleComplete())
            {
            double content;

// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  boolean lookInQueue = false;  // should we even bother to go through the queue?
  if (nextTime > time) 
  t = nextTime;       // consider the possibility of items at next integer timestep
*/
            for(int x=0;x<queue.length;x++)
                if( !queue[x].isEmpty() )
                    {
                    content = queue[x].getMinKey();
                    if (t >= content) 
                        {
                        t = content; 
// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  lookInQueue = true; 
*/
                        }
                    }
        
            if (t==AFTER_SIMULATION) // nothin'...
                return false; 
        
            time = t;
            
            // Extract the contents
            // slightly less efficient than previous version (extraneous call
            // to minKey(), oh well, much simpler to understand)
            for(int x=0;x<queue.length;x++)
                {
                _numSubsteps[x] = 0;
                
// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  if (time == nextTime)
  {
  int len = _numSubsteps[x] + numNext[x];
  if (len > _substeps[x].length)
  _substeps[x] = increaseSubsteps(_substeps[x],len*2+1);  // more than twice needed
  System.arraycopy(next[x],0,_substeps[x], _numSubsteps[x],numNext[x]);
  // we're not zeroing array right now, so stuff can't get GC'd.  :-(
  _numSubsteps[x] += numNext[x];
  numNext[x] = 0;
  }

  if (lookInQueue)
*/
                while ( (!queue[x].isEmpty()) && ((content=queue[x].getMinKey())==time) )
                    {
                    if (_numSubsteps[x] == _substeps[x].length)
                        _substeps[x] = increaseSubsteps(_substeps[x]);
                    _substeps[x][_numSubsteps[x]++] = (Steppable)(queue[x].extractMin());
                    }

                // shuffle the queue _substeps
                Steppable temp;
                final Steppable[] s = _substeps[x];
                int len = _numSubsteps[x]-1;
                for(int z=len; z>0 ;z--)
                    {
                    int i = state.random.nextInt(z+1);
                    temp = s[i];
                    s[i] = s[z];
                    s[z] = temp;
                    }
                }
            
            // execute the content -- we use the retained private
            // local variables defined above just in case the user blows away
            // the substep variables with a resetQueues().
            for(int x=0;x< _substeps.length;x++)
                {
                final int l = _numSubsteps[x];
                for(int z=0;z<l;z++)
                    {
                    if (_substeps[x][z]!=null)
                        {
                        _substeps[x][z].step(state);
                        _substeps[x][z] = null;  // let GC
                        }
                    } 
                }
            }
        else
            {
            time = AFTER_SIMULATION;
            return false;
            }
        steps++;
        return true;
        }
        
    /** Schedules the event to occur at time() + 1.0, 0 ordering. If this is a valid time
        and event, schedules the event and returns TRUE, else returns FALSE.  */
    
    // synchronized so getting the time can be atomic with the subsidiary scheduleOnce function call
    public synchronized boolean scheduleOnce(final Steppable event)
        {
        return scheduleOnce(time+1.0,0,event);
        }
        
    /** Schedules the event to occur at the provided time, 0 ordering.  If the time() == the provided
        time, then the event is instead scheduled to occur at time() + epsilon (the minimum possible next
        timestamp). If this is a valid time
        and event, schedules the event and returns TRUE, else returns FALSE.*/
    
    public boolean scheduleOnce(final double time, final Steppable event)
        {
        return scheduleOnce(time,0,event);
        }
        
    /** Schedules the event to occur at the provided time, and in the ordering provided.  If the time() == the provided
        time, then the event is instead scheduled to occur at time() + epsilon (the minimum possible next
        timestamp). If this is a valid time, ordering,
        and event, schedules the event and returns TRUE, else returns FALSE.
    */
    public synchronized boolean scheduleOnce(double time, final int ordering, final Steppable event)
        {
        // locals are a teeny bit faster
        double thistime = this.time;
        Heap[] queue = this.queue;
        
        if (time == thistime)
            // bump up time to the next possible item.  If time == infinity, this will be bumped to NaN
            time = Double.longBitsToDouble(Double.doubleToRawLongBits(time)+1L);

        if (time < EPOCH || time >= AFTER_SIMULATION || time != time /* NaN */ || time < thistime || event == null || ordering >= queue.length || ordering < 0)
            {
            if (!isThrowingScheduleExceptions()) 
                return false;
            else if (time < EPOCH)
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the time provided ("+time+") is < EPOCH (" + EPOCH + ")");
            else if (time >= AFTER_SIMULATION)
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the time provided ("+time+") is >= AFTER_SIMULATION (" + AFTER_SIMULATION + ")");
            else if (time != time /* NaN */)
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the time provided ("+time+") is NaN");
            else if (time < thistime)
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the time provided ("+time+") is less than the current time (" + thistime + ")");
            else if (event == null)
                throw new IllegalArgumentException("The provided Steppable is null");
            else if (ordering >= queue.length)
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the ordering provided ("+ordering+") is >= the number of orderings (" + queue.length + ")");
            else if (ordering < 0)
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the ordering provided ("+ordering+") is less than zero.");
            }
        
// UNCOMMENT FOR NEXT-STEP FUNCTIONALITY
/*
  final long t = (long)(thistime + 1.0);
  if (time == t && time <= MAXIMUM_INTEGER)  // next one up.  We don't use this beyond MAXIMUM_INTEGER to be safe
  {
  // locals a bit faster
  int[] numNext = this.numNext;
  Steppable[][] next = this.next;

  nextTime = t;
  if (numNext[ordering] == next[ordering].length)  // increase next steps
  next[ordering] = increaseSubsteps(next[ordering]);
  next[ordering][numNext[ordering]] = event;
  numNext[ordering]++;
  }
  else 
*/ 
        queue[ordering].add(event,time);
        return true;
        }
    
    /** Schedules the event to recur at an interval of 1.0 starting at time() + 1.0, and at 0 ordering.
        If this is a valid event, schedules the event and returns a Stoppable, else returns null.
        The recurrence will continue until time() >= AFTER_SIMULATION, the Schedule is cleared out,
        or the Stoppable's stop() method is called, whichever happens first.

        <p> Note that calling stop() on the Stoppable
        will not only stop the repeating, but will <i>also</i> make the Schedule completely
        forget (lose the pointer to) the Steppable scheduled here.  This is particularly useful
        if you need to make the Schedule NOT serialize certain Steppable objects.  <b>Do not use this
        with a real-valued schedule.</b> */
    
    // synchronized so getting the time can be atomic with the subsidiary scheduleRepeating function call
    public synchronized Stoppable scheduleRepeating(final Steppable event)
        {
        return scheduleRepeating(time+1.0,0,event,1.0);
        }

    /** Schedules the event to recur at the specified interval starting at time() + interval, and at 0 ordering.
        If this is a valid interval (must be positive)
        and event, schedules the event and returns a Stoppable, else returns null.
        The recurrence will continue until time() >= AFTER_SIMULATION, the Schedule is cleared out,
        or the Stoppable's stop() method is called, whichever happens first.

        <p> Note that calling stop() on the Stoppable
        will not only stop the repeating, but will <i>also</i> make the Schedule completely
        forget (lose the pointer to) the Steppable scheduled here.  This is particularly useful
        if you need to make the Schedule NOT serialize certain Steppable objects.  <b>Do not use this
        with a real-valued schedule.</b> */
    
    // synchronized so getting the time can be atomic with the subsidiary scheduleRepeating function call
    public synchronized Stoppable scheduleRepeating(final Steppable event, final double interval)
        {
        return scheduleRepeating(time+interval,0,event,interval);
        }

    /** Schedules the event to recur at the specified interval starting at the provided time, and at 0 ordering.
        If the time() == the provided
        time, then the first event is instead scheduled to occur at time() + epsilon (the minimum possible next
        timestamp). If this is a valid time, ordering, interval (must be positive), 
        and event, schedules the event and returns a Stoppable, else returns null.
        The recurrence will continue until time() >= AFTER_SIMULATION, the Schedule is cleared out,
        or the Stoppable's stop() method is called, whichever happens first.
    
        <p> Note that calling stop() on the Stoppable
        will not only stop the repeating, but will <i>also</i> make the Schedule completely
        forget (lose the pointer to) the Steppable scheduled here.  This is particularly useful
        if you need to make the Schedule NOT serialize certain Steppable objects.    <b>Do not use this
        with a real-valued schedule.</b> */

    public Stoppable scheduleRepeating(final double time, final Steppable event)
        {
        return scheduleRepeating(time,0,event,1.0);
        }

    /** Schedules the event to recur at the specified interval starting at the provided time, 
        in ordering 0.  If the time() == the provided
        time, then the first event is instead scheduled to occur at time() + epsilon (the minimum possible next
        timestamp). If this is a valid time, interval (must be positive), 
        and event, schedules the event and returns a Stoppable, else returns null.
        The recurrence will continue until time() >= AFTER_SIMULATION, the Schedule is cleared out,
        or the Stoppable's stop() method is called, whichever happens first.
    
        <p> Note that calling stop() on the Stoppable
        will not only stop the repeating, but will <i>also</i> make the Schedule completely
        forget (lose the pointer to) the Steppable scheduled here.  This is particularly useful
        if you need to make the Schedule NOT serialize certain Steppable objects.    <b>Do not use this
        with a real-valued schedule.</b> */

    public Stoppable scheduleRepeating(final double time, final Steppable event, final double interval)
        {
        return scheduleRepeating(time,0,event,interval);
        }

    /** Schedules the event to recur at an interval of 1.0 starting at the provided time, 
        and in the ordering provided.  If the time() == the provided
        time, then the first event is instead scheduled to occur at time() + epsilon (the minimum possible next
        timestamp). If this is a valid time, ordering,
        and event, schedules the event and returns a Stoppable, else returns null.
        The recurrence will continue until time() >= AFTER_SIMULATION, the Schedule is cleared out,
        or the Stoppable's stop() method is called, whichever happens first.
    
        <p> Note that calling stop() on the Stoppable
        will not only stop the repeating, but will <i>also</i> make the Schedule completely
        forget (lose the pointer to) the Steppable scheduled here.  This is particularly useful
        if you need to make the Schedule NOT serialize certain Steppable objects.    <b>Do not use this
        with a real-valued schedule.</b> */

    public Stoppable scheduleRepeating(final double time, final int ordering, final Steppable event)
        {
        return scheduleRepeating(time,ordering,event,1.0);
        }

    /** Schedules the event to recur at the specified interval starting at the provided time, 
        and in the ordering provided.  If the time() == the provided
        time, then the first event is instead scheduled to occur at time() + epsilon (the minimum possible next
        timestamp). If this is a valid time, ordering, interval (must be positive), 
        and event, schedules the event and returns a Stoppable, else returns null.
        The recurrence will continue until time() >= AFTER_SIMULATION, the Schedule is cleared out,
        or the Stoppable's stop() method is called, whichever happens first.
    
        <p> Note that calling stop() on the Stoppable
        will not only stop the repeating, but will <i>also</i> make the Schedule completely
        forget (lose the pointer to) the Steppable scheduled here.  This is particularly useful
        if you need to make the Schedule NOT serialize certain Steppable objects.    <b>Do not use this
        with a real-valued schedule.</b> */

    public Stoppable scheduleRepeating(final double time, final int ordering, final Steppable event, final double interval)
        {
        if (event==null || interval < 0 || interval != interval /* NaN */) // don't check for interval being infinite -- that might be valid!
            {
            if (!isThrowingScheduleExceptions())
                return null;  // 0 is okay because it's "the immediate next"
            else if (event == null)
                throw new IllegalArgumentException("The provided Steppable is null");
            else if (interval < 0)
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the interval provided ("+interval+") is less than zero");
            else if (interval != interval)  /* NaN */
                throw new IllegalArgumentException("For the Steppable...\n\n"+event+
                                                   "\n\n...the interval provided ("+interval+") is NaN");
            }
        Repeat r = new Repeat(event,interval,ordering);
        if (scheduleOnce(time,ordering,r)) return r; 
        else return null;
        }
    
    /**
       Handles repeated steps.  This is done by wrapping the Steppable with a Repeat object
       which is itself Steppable, and on its step calls its subsidiary Steppable, then reschedules
       itself.  Repeat is stopped by setting its subsidiary to null, and so the next time it's
       scheduled it won't reschedule itself (or call the subsidiary).   A private class for
       Schedule.  */
    
    class Repeat implements Steppable, Stoppable
        {
        protected double interval;
        protected Steppable step;  // if null, does not reschedule
        protected int ordering;
        
        public Repeat(final Steppable step, final double interval, final int ordering)
            {
            this.step = step;
            this.interval = interval;
            this.ordering = ordering;
            }
        
        public synchronized void step(final SimState state)
            {
            if (step!=null)
                {
                // this occurs WITHIN the schedule's synchronized step, so time()
                // and scheduleOnce() will both occur together without the time
                // changing
                try
                    {
                    scheduleOnce(time()+interval,ordering,this);
                    }
                catch (IllegalArgumentException e) { /* Only occurs if time has run out */}
                step.step(state);
                }
            }
        
        public synchronized void stop()  
            {
            step = null;
            }

        // explicitly state a UID in order to be 'cross-platform' serializable 
        // because we ARE an inner class and compilers come up with all sorts
        // of different UIDs for inner classes and their parents.
        static final long serialVersionUID = 2562838695289414534L;
        }
        
    // explicitly state a UID in order to be 'cross-platform' serializable
    // because we contain an inner class and compilers come up with all
    // sorts of different UIDs for inner classes and their parents.
    static final long serialVersionUID = -7903946075763886169L;
    }
    


