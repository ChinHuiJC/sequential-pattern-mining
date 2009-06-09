import java.io.* ;
import java.util.* ;
import java.lang.Math ;

public class SPAM_v2{
	private int powk ;
	private int cluster ;
	private int maxTransSize ;
	private int T ;
	private String tmp_file ;
	private boolean enable_tree ;
	private boolean enable_hash ;
	
	private Hashtable hash = new Hashtable() ;
	private Hashtable hash_value = new Hashtable() ;
	private Hashtable hash_pos = new Hashtable() ;
	List<String> seed = new ArrayList<String>();

	public boolean createLevel(){
		boolean hasNext = false ;
		try{
			FileWriter fwer = new FileWriter(tmp_file+".next") ;
			// read tmp_file
			BufferedReader in = new BufferedReader(new FileReader(tmp_file));
			String s = null ;
			while( (s=in.readLine()) != null ){
				int is_seed = 0 ;	
				// string to array
				String[] s2 = s.split("\t") ;
				String node = s2[0] ;
				boolean[] node_array = stringToArray(s2[1]) ;
				// array is ready
				for(String sd : seed){
					boolean[] seed_array = getHash(sd) ;

					// S_STEP
					boolean[] result = s_step( node_array , seed_array) ;
					String newnode = node+"->"+sd ;
					// put value hash
					int value = sumArray(result) ;
					if(enable_hash)
						addValue( newnode , value ) ;
				
					// maxTrans & set condition
					if((newnode.split("->")).length < maxTransSize && value >= T ){
						// has next node
						hasNext = true ;

						fwer.write(newnode+"\t") ;
						printFileArray(fwer , result) ;
					}
					
					// I_STEP
					String[] i_string = node.split("->") ;
					String i_string_part = i_string[i_string.length-1] ;
					String[] i_element = i_string_part.split(":") ;
					String i_string_ele = i_element[i_element.length-1] ;
					if(i_string_part.indexOf(sd) == -1 && seed.indexOf(i_string_ele) < seed.indexOf(sd) ){
						result = i_step( node_array , seed_array) ;
						newnode = node+":"+sd ;
						// put hash
						value = sumArray(result) ;
						if(enable_hash)
							addValue( newnode , value ) ;
						// set condition
						if( value >= T) {
							hasNext = true ;
							fwer.write(newnode+"\t") ;
							printFileArray(fwer,result) ;
						}
					}
					if( sd.equals(node) )
						is_seed = 1 ;
				}
				if( is_seed == 0 )
					removePos(node) ;
			}
			fwer.close();
		}catch(IOException e){
			e.printStackTrace() ;
		}

		// temp_file.next -> temp_file
		if(hasNext){
			File f = new File(tmp_file+".next");
			f.renameTo(new File(tmp_file)) ;	
		}

		return hasNext ;
	}

	public void buildTree() {
		try{	
			FileWriter fw = new FileWriter(tmp_file); 
			// add seed to tmp_file
			for(String s : seed){
				// Value Hash table
				int value = sumArray(getHash(s)) ;
				if(enable_hash)
					addValue( s , value ) ;
				// Print to tmp_file
				printNode(fw , s) ;
			}
			fw.close() ;		
		}catch(IOException e){
			e.printStackTrace() ;
		}
		// creat level 
		int level = 1 ;
		boolean conti = true ;
		while( conti ){
			 long start , duration ;
			 start = System.nanoTime() ;
			conti = createLevel() ;
			 duration = System.nanoTime() - start ;
		 	 System.err.println( "\tlevel "+level+":"+ duration/1e+9 + " sec");
			level++ ;
		}
	}

	public boolean[] stringToArray(String s){
		boolean[] array = new boolean[powk*cluster] ;

		String[] s_tmp = s.split(" ") ;

		for(int i=0 ; i<array.length ; i++){
			array[i] = Boolean.valueOf(s_tmp[i]) ;
		}
		return array ;
	}

	public void printNode(FileWriter fw , String s){
		boolean[] array = getHash(s) ;
		try{
			fw.write(s+"\t");
			printFileArray(fw , array) ;	
		}catch(IOException e){
			e.printStackTrace() ;
		}
	}

	public void printFileArray(FileWriter fw , boolean[] array){
		try{
			for(int i=0 ; i<array.length ; i++ ){
				fw.write(array[i]+" ") ;	
			}
			fw.write("\n") ;
		}catch(IOException e){
			e.printStackTrace() ;
		}
	}

	public void setEnableTree(boolean b){
		enable_tree = b ;
	}
	public void setEnableHash(boolean b){
		enable_hash = b ;
	}
	public void setT(int t){
		T = t ;
	}
	public void setPowk(int k){
		powk = k ;
	}
	public void setMaxTransSize(int c){
		maxTransSize = c;
	}

	public void setCluster(int c){
		cluster = c ;
	}
	public void removeHash(String key) {
		hash.remove(key) ;
	}
	public void removeValue(String key){
		hash_value.remove(key) ;
	}
	public void removePos(String key){
		hash_pos.remove(key) ;
	}
	public void addHash(String key){
		boolean[] array = new boolean[powk*cluster] ;
		Arrays.fill(array, false) ;
		hash.put(key , array) ;
	}
	public void addHash(String key, boolean[] array){
		hash.put(key , array) ;
	}
	public void addValue(String key, int i){
		hash_value.put(key , new Integer(i)) ;
	}

	public Integer getValue(String key){
		return (Integer) hash_value.get(key) ;
	}
	public boolean[] getHash(String key){
		return (boolean[]) hash.get(key) ;
	}
	public void printHash(){
		Set s ;
		s = hash.keySet() ;

		Iterator<String> it = s.iterator();

		while ( it.hasNext() == true )
		{
			String key = it.next();
			System.out.print( key+"\t" );

			boolean[] t =(boolean[]) hash.get(key) ;

			for(int i=0 ; i<t.length ; i++){
				if(i%powk == 0){
					System.out.print("#") ;
				}

				System.out.print(t[i]+" ") ;
			}
			System.out.print("# Sum: ") ;
			System.out.print( sumArray(t) ) ;
			System.out.print("\n") ;
		}

	}
	public void printBitMap(){
		System.out.println(cluster + " " + powk) ;
		Set s ;
		s = hash.keySet() ;
		
		Iterator<String> it = s.iterator();

		while( it.hasNext() ){
			String key = it.next();
			System.out.print( key+"\t" );

			boolean[] t =(boolean[]) hash.get(key) ;
			for(int i=0 ; i<t.length ; i++){
				if(t[i])
					System.out.print("1 ") ;
				else
					System.out.print("0 ") ;
			}
			System.out.print("\n") ;
		}
	}

	public void printArray(boolean[] array){

		for(int i=0 ; i<array.length ; i++){
			System.out.print(array[i]+" ") ;
		}
		System.out.print("\n") ;
	}

	public boolean hasKeyHash(String s){
		return hash.containsKey(s) ;
	}

	public boolean[] s_step_process(boolean[] data){
		boolean[] answer = new boolean[powk*cluster] ;

		for(int base=0 ; base<powk*cluster ; base+=powk) {
			boolean flag=false;
			for(int offset=0 ; offset<powk ; offset++){
				int index = base+offset ;
				if(flag){
					answer[index] = true ;	
				}else{ //flag==0
					if(data[index] == true){
						flag=true ;
					}
					answer[index] = false ;
				} 
			}	
		}
		return answer ;
	}

	public boolean[] s_step(boolean[] from , boolean[] to){
		// s_process
		boolean[] answer = s_step_process(from) ;

		// AND array
		for(int i=0 ; i<answer.length ; i++)
			answer[i] = answer[i] & to[i] ;

		return answer ;
	} 
	public boolean[] i_step(boolean[] from , boolean[] to){
		boolean[] answer = new boolean[powk*cluster] ;
		// AND array
		for(int i=0 ; i<from.length ; i++){
			answer[i] = from[i] & to[i] ;
		}
		return answer ;
	} 

	public int sumArray(boolean[] array){
		int sum=0 ;
		for(int i=0 ; i<array.length ; i++){
			if(array[i])
				sum+=1;
		}

		return sum ;
	}

	public void setTmpFile(String tmp){
		tmp_file = tmp ;
	}
/*
	public void setParameter(String input) {
	try{
		BufferedReader in = new BufferedReader(new FileReader(input));
		String s = null ;
		int max_cluster_size=0 ;
		int total_cluster=0 ;
		
		int pre_id=-1 ;
		int cluster_size=0 ;
		while( (s=in.readLine())!= null ){
			String[] com = s.split("\t") ;
			
			int cluster_id = Integer.valueOf(com[0]);
			// new cluster
			if(cluster_id != pre_id){
				if(cluster_size>max_cluster_size)
					max_cluster_size = cluster_size ;
				cluster_size=0 ;
				
				total_cluster++ ;
			}

			pre_id = cluster_id ;
			cluster_size++ ;
		}
		if(cluster_size>max_cluster_size)
			max_cluster_size = cluster_size ;

		// compute k
		double k = (int)Math.ceil(Math.log(max_cluster_size)/Math.log(2)) ;

		// set Parameter
 		setCluster(total_cluster) ;
		setMaxTransSize(max_cluster_size) ;
		setPowk((int)Math.pow(2,k)) ;	
	}catch(IOException e){
		e.printStackTrace() ;
	}


	}
*/
/*
	public SPAM_v2(String input){
		// default values
		setParameter(input) ;
		System.err.println("powk=" +powk + " cluster=" + cluster + " maxTransize=" + maxTransSize) ;
		// reading data
		try {
			BufferedReader in = new BufferedReader(new FileReader(input));

			String s = null ;
			int offset = 0 ; // offset for each cluster
			int pre_id = -1 ; 
			while( (s = in.readLine()) != null ){
				String[] component = s.split("\t") ; //for different input
				
				int cluster_id = Integer.valueOf(component[0]);
				String data = component[2] ;

				if(cluster_id != pre_id){ // new cluster
					offset=0 ;
				}
				int index = cluster_id*(this.powk) + offset ;

				// insert bitmap	
				String[] point = data.split(",") ;
				
				for(int i=0 ; i<point.length ; i++){
					if(!hasKeyHash(point[i])){
						addHash(point[i]) ;
						seed.add(point[i]) ;				
					}
					boolean[] array = getHash(point[i]) ;
					array[index]=true; 
				}
				offset++ ;	
				pre_id = cluster_id ;
			}
		}catch(IOException e){
			e.printStackTrace() ;
		}
	}
*/
/*
	public static void main(String[] args){
		// Deal with arguments
		String input_file=null ;
		String tmp_file=null ;
		int T = 0 ;
		boolean enable_tree = false ;
		boolean enable_hash = false ;
		boolean enable_bitmap = false ;
		for(int i=0 ; i<args.length ; i++){
			if( "-i".equals(args[i]) )
				input_file = args[++i] ;
			if( "-t".equals(args[i]) )
				T=Integer.valueOf(args[++i]) ;
			if( "-tmp".equals(args[i]) )
				tmp_file = args[++i] ;
			if( "-tree".equals(args[i]))
				enable_tree = true ;
			if( "-hash".equals(args[i]) )
				enable_hash = true ;
			if( "-bitmap".equals(args[i]) )
				enable_bitmap = true ;
		}

		// initial input
		long start , duration ;

		// Constructor
		 start = System.nanoTime();
		SPAM_v2 spam = new SPAM_v2(input_file) ;
		 duration = System.nanoTime() - start ;
		 System.err.println( "Build BitMap: "+ duration/1e+9 + " sec");

		spam.setT(T) ;
		spam.setTmpFile(tmp_file) ;
		spam.setEnableTree(enable_tree) ;
		spam.setEnableHash(enable_hash) ;

		if(enable_bitmap){
			spam.printBitMap() ;
			return ; 
		}

		// Build Tree
		 start = System.nanoTime();
		 System.err.println("Build Tree: ") ;
		spam.buildTree() ;
		 duration = System.nanoTime() - start ;
		 System.err.println( "Build Tree Total: "+ duration/1e+9 + " sec");

		if(enable_tree)		
			spam.printDispTree() ;

		//pause
		try{
			System.err.println("finished.") ;
			System.in.read();
		}catch(Exception e){};
	}		
*/
}
