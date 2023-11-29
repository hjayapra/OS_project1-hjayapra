import java.util.*; 
import java.io.*; 


public class vmsim
{    
	    //init statistic var and cmd var 
	    public static Scanner myscan;
	    public static FileInputStream fis;
	    public static String traceFile = null; 
	    public static String algType = null; 
	    public static int numFrames =0; 
	    public static int pageSize = 0; 
	    public static int memAccess = 0; 
	    public static int pageFaults = 0; 
	    public static int diskWrites = 0; 
	    public static int memSplit1 = 0; 
	    public static int memSplit2 = 0;
	    //init process info 
	    public static int offset = 0; 
	    public static int line = 0;
	    //init attrib for process and pagetableentry
	    public static boolean dirty_bit = false; 

    interface ObjectSim{
    	//prints out the measures 
			public String toPrint(); 
	    	//a single memory access LRU
			public void pageSim(char mode, long address, int process, int line);
			//a single memory access OPT
			//public void OPTobject(int numFrames, int pgSize, int a, int b, FileInputStream fis, int pgoffset);
	  }

  //LRU class 
    class LRUobject implements ObjectSim
    {
	//init an array to store the two processes
    LRUproc processArr[] = new LRUproc[2]; 
    //init var for stats  
    int pageSize; 
    int pageFaults;  
    int diskWrites; 
    int memAccess; 
    int numFrames; 
    String algtype;  

   	 public LRUobject(String algType, int frames, int pgSize, int a, int b)
     {
     	 
          pageSize = pgSize;
          numFrames = frames; 
          algtype  = algType; 
          //set all measures to 0 
		  		pageFaults = 0; 
          diskWrites = 0;
          memAccess = 0;
         //store into the process array 
         int i = 0; 
         while(i < processArr.length-1)
         {   
         	  int p = processA(frames, a, b); 
         	  int k = processB(frames, a, b); 
         		processArr[i] = new LRUproc(p); 
         		processArr[i+1] =  new LRUproc(k); 
         		i++; 
         }
		 
     }
     //calculates process A frame allocation 
     public int processA(int totalframes, int a, int b){
       
       int processA_alloc = ((totalframes/(a + b)) * a); 
	  	 return processA_alloc;  

	}
	//calculates process B frame allocation 
	public int processB(int totalframes, int a, int b){

        int processB_alloc = ((totalframes/(a + b)) * b); 
				return processB_alloc; 

	}
	//check to see if the current process is in memory if it is change the dirty bit to true 
	// if the process is not in memory add it 
    public void pageSim(char mode, long address, int process, int line)
    {
           pageTableEntry currPage = null; 
    	  //Maintain a counter for each time a memacc for a page occurrs 
           memAccess++;
           //set the current process 
           LRUproc currProcess = processArr[process];
           //check if the page is in memory if pageValid returns true the page is in memory 
           boolean pageValid; 
           pageValid = isFound(currProcess, address);
           //get the pointer to the page if it exists in memory 
           currPage = findPage(currProcess, address);
           //if the page is in memory  
           if(pageValid)
           {
           	    if(mode == 's') 
           	    {
           	    	currPage.dirty_bit = true; 
           	    }
           }
           //if the page is not in memory inc page fault and add the page to the table
           else
           {
           	    boolean spaceValid;
           	    pageFaults++; 
           	    spaceValid = memSpace(currProcess); 
           	    currPage = new pageTableEntry(address); 
           	    if(mode == 's')
           	    {  
           	    	//set the dirty bit to true
           	    	currPage.dirty_bit = true; 
           	    }
           	    //if there is no space available in the table evict the first page
           	    // set the dirty bit to true 
           	    //inc the diskWrite and add the page to the table
           	    if(!spaceValid)
           	    {
           	    	pageTableEntry curr = currProcess.mem.remove(0); 
           	    	if(curr.dirty_bit == true)
           	    	{
           	    		diskWrites++; 
           	    	}
           	    	currProcess.mem.add(currPage); 
           	    }
           	    //if space available add it to the table 
           	    else{
           	    	currProcess.mem.add(currPage); 
           	    	currProcess.currPages++; 
           	    }
           
           } 	   
    }
    //method to check the available space in ram 
	public boolean memSpace(LRUproc proc)
	{
		if(proc.currPages < proc.capacity)
			return true;
		else 
			return false;  
	}
	//calls findPage but returns a boolean if the page is in memory 
	public boolean isFound(LRUproc proc, long pageNum){

		pageTableEntry temp = findPage(proc, pageNum); 
		if(temp == null)
			return false;
		else 
			return true;  
	}
	//returns a pointer to the page if found 
	public pageTableEntry findPage(LRUproc proc, long pageNum)
	{
		int i = 0; 
		if(!proc.mem.isEmpty())
		{
			 //while not at the end of mem look through to find page
		    while(i < proc.mem.size())
			  { 
								//set the current page 
								pageTableEntry currPage = proc.mem.get(i); 
                if(currPage.pageNum == pageNum)
                	{ 
                	  //when the page has been access move it to the back of the list
                      pageTableEntry lastproc = proc.mem.remove(i); 
                      proc.mem.add(lastproc);
                      //return pointer to page 
                       return currPage; 
                	}
				   i++;
			}
		}
		return null; 
	}
	public String toPrint()
	{
		return "Algorithm: " + algtype.toUpperCase() + "\n" +
           "Number of frames: " + numFrames + "\n" +
           "Page size: " + pageSize + " KB\n" +
           "Total memory accesses: " + memAccess + "\n" +
           "Total page faults: " + pageFaults + "\n" +
           "Total writes to disk: " + diskWrites;
	}
	//page table entry Class 
	public class pageTableEntry{
    	    //init var 
    				long pageNum; 
            boolean dirty_bit; 
            //init var for OPT
            int furthestaccess; 
            LinkedList<Integer> accessed; 
    		//method calls for pagetableentry 
    		//construc for LRU 
    		public pageTableEntry(long pgnum)
    		{
                dirty_bit = false;
                pageNum = pgnum; 
    		}
             //print out stats 
    		 public String toPrint()
   			 {
       			 return String.valueOf(pageNum);
    		 }

    }
	   // LRU process inner class 
     private class LRUproc
     {
        //max amount of memory 
    		private long capacity; 
    	 // linked list to implement memory size for pagetable entries 
    		private LinkedList<pageTableEntry> mem; 
    		private int currPages; 
    		private LRUproc(int frames)
    	{
    		  //init the capacity to the number of frames 
    		  // currPages are the current pages loaded in by a process
              capacity = frames; 
              mem = new LinkedList<pageTableEntry>(); 
              currPages	= 0; 
    	}
    }
}

// LRU CLASS

//OPT CLASS
  
	class OPTobject implements ObjectSim{
		 //init var 
	    int pageSize; 
	    int pageFaults;  
	    int diskWrites; 
	    int memAccess; 
	    int numFrames; 
	    String algtype;
	    Scanner scan;
	    //OPTProc arr to hold the two processes 
	    OPTproc[] processArr = new OPTproc[2];   
        //throwing a warning for file
		@SuppressWarnings("unchecked")
		public OPTobject(String algType, int frames, int pgSize, int a, int b, FileInputStream fis, int pgoffset)
		{
		  //get values and alg type 
           algtype = algType; 
           numFrames = frames; 
           pageSize = pgSize; 
          //init scanner 
           scan = new Scanner(fis);
           //fill the Array from the traceFile 
           int counter = 0; 
           Hashtable<Long, pageTableEntry> procMem[] = new Hashtable[2];
           //hashtable for the two processes 
           procMem[0] = new Hashtable<Long,pageTableEntry>(); 
           procMem[1] = new Hashtable<Long,pageTableEntry>(); 
           while(scan.hasNextLine())
           {
	            //arr to hold the data from each line of the file 
	           	String[] dataProc = scan.nextLine().split(" ");
	           	//process 0 or 1 
	           	int process = Integer.parseInt(dataProc[2]); 
	           	//get address using the offset 
	           	Hashtable<Long, pageTableEntry> curr = procMem[process]; 
	           	long address = Long.decode(dataProc[1]); 
	           	address = getLineAddress(address, pgoffset); 
	            //check if the current page is in the hash table 
	            boolean isValid;    
	            pageTableEntry currPage; 
	     				currPage = findPageNum(curr, address); 
	            isValid = isPageNumValid(curr, address);
	            if(isValid)
	            {
	            	 //dd the current line to the Page table entry 
	            	  currPage.accessed.add(counter); 
	            }
	            else
	            {
	            	   //add a new page to the page table entry 
	            	   //add it to the ptaccess
	            	    currPage = new pageTableEntry(counter, address); 
	            	    currPage.accessed.add(counter);
	            	    curr.put(address, currPage);
	            }

	     		   	counter++; 
           }
          
          //calculate number of frames from the mem split  
          //store into the process array 
          int i = 0; 
          while(i < processArr.length-1)
         {   
         	    int p = processA(frames, a, b); 
         	    int k = processB(frames, a, b); 
	         	  processArr[i] = new OPTproc(p, procMem[0]); 
	         	  processArr[i+1] = new OPTproc(k, procMem[1]);
	         		i++; 
         }
         //set measures to 0 
          memAccess = 0; 
          pageFaults = 0; 
          diskWrites = 0; 

		}
		public long getLineAddress(long address, int offset)
		{
				address = address >> offset; 
				return address; 

		}
		//calculates process A frame allocation 
     	public int processA(int totalframes, int a, int b){
       
       		int processA_alloc = ((totalframes/(a + b)) * a); 
	   			return processA_alloc;  

		}
		//calculates process B frame allocation 
		public int processB(int totalframes, int a, int b){

        	int processB_alloc = ((totalframes/(a + b)) * b); 
        	return processB_alloc; 

		}
		public boolean isPageNumValid(Hashtable<Long, pageTableEntry> temp, long address)
		{
			 pageTableEntry currP= findPageNum(temp, address); 
			 if(currP == null)
			 	return false; 
			 else
			 	return true; 
		}
		public pageTableEntry findPageNum(Hashtable<Long, pageTableEntry> temp, long address)
		{
			pageTableEntry pageEntry = temp.get(address); 
			return pageEntry; 

		}
		public void pageSim(char mode, long address, int process, int line)
		{ 
			  memAccess++; 
			  
        
		}
		public pageTableEntry getPointertoPTE(OPTproc temp, long address)
		{
			pageTableEntry curr = temp.ptaccess.get(address); 
			return curr; 
		}
		public boolean currProinMem(OPTproc temp, long address)
		{
			if(temp.mem.contains(address))
				return true; 
			else
				return false; 
		}
		public Long getEvictedPage(OPTproc proc)
		{
			LinkedList<pageTableEntry> notAccessed = new LinkedList<>(); 
			Long evict = null; 
			int currAccess = -9999999; 
			//mem ArrayList 
			for(int i = 0; i < proc.mem.size(); i++)
			{
				pageTableEntry accessNum = proc.ptaccess.get(i); 
        //if there are no more accesses add the page to the Linked List 
				if(accessNum.accessed.isEmpty())
				{
					 notAccessed.add(accessNum); 
				}
				else
				{
					 boolean latestAccess = nextAccess(proc, i, currAccess); 
					 if(latestAccess)
					 {
					 	 evict = accessNum.pageNum; 
					 	 currAccess = accessNum.accessed.get(0); 
					 }
				}
			}
			//check if there are processes without any accesses 

			return evict; 
		}
		public boolean nextAccess(OPTproc temp, int p, int minValue)
		{
			pageTableEntry curr = temp.ptaccess.get(p); 
			if(curr.accessed.get(0) > minValue) 
			{
					return true; 
			}
			return false; 
       
		}
		public boolean spaceAvail(OPTproc temp)
		{
       if(temp.mem.size() >= temp.capacity)
       	return false; 
       return true; 
		}
	
		public String toPrint()
		{
			return "Algorithm: " + algtype.toUpperCase() + "\n" +
                "Number of frames: " + numFrames + "\n" +
                "Page size: " + pageSize + " KB\n" +
                "Total memory accesses: " + memAccess + "\n" +
                "Total page faults: " + pageFaults + "\n" +
                "Total writes to disk: " + diskWrites; 
		} 
		public class pageTableEntry
		{
    	    //init var 
    				long pageNum; 
            boolean dirty_bit; 
            //init var for OPT
            int furthestaccess; 
            LinkedList<Integer> accessed; 
    		//construc OPT pagetableentry 
    		public pageTableEntry(int acctime, long pgNum)
    		{
               pageNum = pgNum; 
               furthestaccess = acctime; 
               dirty_bit = false; 
               accessed = new LinkedList<Integer>();
    		}
             //print out stats 
    		 public String toPrint()
   			 {
       			 return String.valueOf(pageNum);
    		 }

    }

 //OPT process inner class 
    private class OPTproc{ 
        //ArrayList of current pages in memory 
    		private ArrayList<Long> mem; 
    		//max size set by frame number  
        private int capacity;  
        //Hashtable of the line access per page 
        private Hashtable<Long, pageTableEntry> ptaccess; 
       

    	private OPTproc(int frames, Hashtable<Long, pageTableEntry> table)
    	{
    		//init var 
    	 	mem = new ArrayList<>(); 
    	 	ptaccess = table; 
    		capacity = frames;  
    		  
    	}
    }

}


// OPT CLASS

	public static void main (String[] args)
	{
        //set up read from keyboard ; scanner to read from file
        //Read the data from cmd line 
        ///vmsim -a <opt|lru> –n <numframes> -p <pagesize in KB> -s <memory split> <tracefile>
        // System.out.println("Please enter the specifications for vmsim in the format -a <opt|lru> -n <numframes> -p <pagesize in KB> -s <memory split> <tracefile>"); 
        //loop through the cmd line and store the values into appro var 
        
         int i = 0; 
         while(i < args.length)
         {
         		if(args[i].equals("-a")){
                    algType = args[i+1]; 
         		}
         		else if(args[i].equals("-n")){
         			//string to Int 
         			 numFrames = Integer.parseInt(args[i+1]); 
         		}
         		else if(args[i].equals("-p")){
         			 pageSize = Integer.parseInt(args[i+1]); 
         		}
         		else if(args[i].equals("-s")){
         			   //input format g:k 
         			   //store into array and then store indiv value 
         			String memArr[] = (args[i+1]).split(":"); 
         			for(int j = 0; j < memArr.length; j++)
         			{
         				memSplit1 = Integer.parseInt(memArr[j]); 
         				memSplit2 = Integer.parseInt(memArr[j+1]);
         				break;  
         			}
         			//page offset in number of hex digits in the trace file 
         	     	//log base 2 of the page size + 10 units are in KB 
                    int convertBase = (int)(Math.log(pageSize) / Math.log(2));  
 				 						offset = (int) Math.ceil(((convertBase) + 10));
         		}
         	    else if(i == args.length -1){
         	    	//trace file 
         	    	try{

         	    			traceFile = args[i];
         	    		  fis= new FileInputStream(traceFile); 
         	    			myscan = new Scanner(fis); 
         	    			
         	    	}
         	    	catch(FileNotFoundException e)
        	       {
	            			System.out.println("File not found please renter file params");
	            			System.exit(0);
                 }
         	    	
         	    }
                 
         	i++; 


         }
	        ObjectSim processSim = null;
	       //System.out.println("success");
	      		if(algType.equals("opt"))
	      		{
	      			//processFormat OPT 
	      			processSim = new vmsim().new OPTobject(algType, numFrames, pageSize, memSplit1, memSplit2, fis, offset); 	

	      		}
	      		if(algType.equals("lru"))
	      		{
	      			//processFormatlru 
	      			processSim = new vmsim().new LRUobject(algType, numFrames, pageSize, memSplit1, memSplit2);
	      		}
        			
	       //print lines for debugging to read in values correct
	       //System.out.println(algType);
	       //System.out.println(numFrames); 
	       //System.out.println(pageSize); 
	       //System.out.println(memSplit1); 
	       //System.out.println(memSplit2);
	       //System.out.println(traceFile);
	        
	       //file line information 
	       //(mode is l or s)
	       // hex address use offset to get address in pagetable 
	       // process either 0 or 1 
	       char mode = 0; 
	       long address = 0; 
	       int process = 0;
         
        //4.) Set up the read from file and store into var
				while(myscan.hasNextLine())
				{
					// Store data in an array so we can access each process' data 
					String[] processData = myscan.nextLine().split(" ");
					//store the access mode (load or store) 
					mode = processData[0].charAt(0); 
					//System.out.println(mode); 
					//convert address to long, and right shift by the offset
					address = Long.decode(processData[1]);  
	        address = address >>> offset; 
	        //System.out.println(address); 
	        //convert the process to an int 0 or 1
	        process = Integer.parseInt(processData[2]); 
	        //System.out.println(process); 
	        //running a single process 
	        processSim.pageSim(mode, address, process, line);
	        line++;  
				}
					//print out pageSim (opt or lru) stats
		 
					System.out.println(processSim.toPrint()); 
					myscan.close();
    //End of Main     
	}
}

		

		 

