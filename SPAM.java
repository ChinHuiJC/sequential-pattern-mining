import java.io.*;
import java.util.*;
import java.net.URI ;
import org.apache.hadoop.fs.* ;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class SPAM extends Configured implements Tool {
	// Mapper
	public static class Map extends MapReduceBase implements Mapper {
		private List<String> seed = new ArrayList<String>();
		private Hashtable hash = new Hashtable();
		private String taskId;
		private int maxTransSize ;
		private int T ;

		// get taskID
		public void configure(JobConf job) {
			taskId = job.get("mapred.task.id");

			super.configure(job);
			T = job.getInt("T",0);
			maxTransSize = job.getInt("maxTS",3);
		}

		public void map(Object key, Object value, OutputCollector output, Reporter reporter) throws IOException {
			// clear
			hash.clear();
			seed.clear();

			// build seed hash table
			String seed = value.toString() ;
			seedToHash(seed) ;
		
			// buildTree
			//buildTree_file(seed, output) ;
			buildTree_memory(seed , output) ;
		}

		// build tree memory
		public void buildTree_memory(String seed , OutputCollector output ){
			try{
			 List<String> cur_node = new ArrayList<String>() ;

			 String[] lists = seed.split("\t") ;
			 for(String s : lists){
				 String[] tmp = s.split(":") ;	 
				 cur_node.add(tmp[0]) ;
				 this.seed.add(tmp[0]) ;
			 }
		
			 //create level
			 int level=0 ;
			 while( !cur_node.isEmpty() ){
				 output.collect(new Text("level "+level) , new IntWritable(0)) ;
				 level++ ;
				 cur_node = createLevel(output, cur_node) ;
			 }
			}catch(IOException e){
				e.printStackTrace() ;
			}				
		}
		
		public List<String> createLevel(OutputCollector output , List<String> cur_node){
			List<String> new_node = new ArrayList<String>() ;
			try{
				for(String node : cur_node) {
					int is_seed=0 ;
					for(String sd : seed){
						//S_STEP
						boolean[] result = s_step(getHash(node) , getHash(sd)) ;
						String newnode = node+"->"+sd ;

						//OUTPUT result
						int value = sumArray(result) ;
						
						// each map shouldn't decide T
						output.collect(new Text(newnode) , new IntWritable(value)) ;

						//NEXT NODE
						String[] leaf = (newnode).split("->") ;
						//if(leaf.length < maxTransSize && value >= T ){
						if(leaf.length < maxTransSize ){
							addHash( newnode , result) ;
							new_node.add(newnode) ;
						}

						//I_STEP
						String[] i_string = node.split("->") ;
						String i_string_part = i_string[i_string.length-1] ;
						String[] i_element = i_string_part.split(":") ;
						String i_string_ele = i_element[i_element.length-1] ;
						if(i_string_part.indexOf(sd) == -1 && seed.indexOf(i_string_ele) < seed.indexOf(sd) ){
							result = i_step( getHash(node) , getHash(sd)) ;
							newnode = node+":"+sd ;

							value = sumArray(result) ;

							// each map shouldn't decide T
							output.collect(new Text(newnode) , new IntWritable(value)) ;
							
							//NEXT NODE
							//if( value >= T){
								addHash(newnode,result) ;
								new_node.add(newnode) ;
							//}
						}

						//CHECK SEED
						if( sd.equals(node) )
							is_seed = 1 ;
					}
					if( is_seed==0 )
						removeHash(node) ;
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			return new_node ;
		}
		// build tree
		public void buildTree_file(String seed, OutputCollector output) {
			try{
				//initial file
				File f = new File(taskId);
				f.delete() ;
				FileWriter fw = new FileWriter(taskId);
				// add seed to tmp_file
				String[] lists = seed.split("\t") ;
				for(String s : lists){
					String[] tmp = s.split(":") ;
					fw.write(tmp[0]);
					fw.write("\t");
					fw.write(tmp[1]);
					fw.write("\n");

					this.seed.add(tmp[0]) ;
				}
				fw.close();

				//create level
				boolean conti=true ;
				int level=0 ;
				while(conti){
					conti = createLevel(output , seed) ;
				}


			}catch(IOException e){
				e.printStackTrace();
			}		
		}



		public boolean createLevel(OutputCollector output , String seed){
			boolean hasNext = false ;
			try{
				FileWriter fwer = new FileWriter(taskId+".next") ;
				//read tmp file
				BufferedReader in = new BufferedReader(new FileReader(taskId));
				String s = null ;
				while( (s=in.readLine()) != null ){
					// string to array
					String[] s2 = s.split("\t") ;
					String node = s2[0] ;
					boolean[] node_array = stringToArray(s2[1]) ;
					// array is ready

					for(String sd : this.seed){
						boolean[] seed_array = getHash(sd) ;
						//S_STEP
						String newnode = node+"->"+sd ;
						boolean[] result = s_step( node_array , seed_array) ;
						int value = sumArray(result) ;
						//output
						output.collect(new Text(newnode) , new IntWritable(value)) ;

						//set condition	
						if((newnode.split("->")).length <= maxTransSize && value >= T ){
							hasNext = true ;

							fwer.write(newnode+"\t") ;
							printFileArray(fwer , result) ;
						}

						//I_STEP
						String[] i_string = node.split("->") ;
						String i_string_part = i_string[i_string.length-1] ;
						String[] i_element = i_string_part.split(":") ;
						String i_string_ele = i_element[i_element.length-1] ;
						if(i_string_part.indexOf(sd) == -1 && seed.indexOf(i_string_ele) < seed.indexOf(sd) ){
							result = i_step( node_array , seed_array) ;
							newnode = node+":"+sd ;
							value = sumArray(result) ;

							//output
							output.collect(new Text(newnode) , new IntWritable(value)) ;

							//set condition
							if( value >= T) {
								hasNext = true ;
								fwer.write(newnode+"\t") ;
								printFileArray(fwer,result) ;
							}
						}
					}
				}
				fwer.close();
			}catch(IOException e){
				e.printStackTrace();
			}

			if(hasNext){
				File f = new File(taskId+".next");
				f.renameTo(new File(taskId)) ;
			}

			return hasNext ;
		}


		public boolean[] i_step(boolean[] from , boolean[] to){
			boolean[] answer = new boolean[from.length] ;

			for(int i=0 ; i<from.length ; i++)
				answer[i] = from[i] & to[i] ;

			return answer ;
		}

		public boolean[] s_step(boolean[] from , boolean[] to){
			boolean[] answer = s_step_process(from) ;

			for(int i=0 ; i<answer.length ; i++)
				answer[i] = answer[i] & to[i] ;

			return answer ;
		}

		public boolean[] s_step_process(boolean[] data){
			boolean[] answer = new boolean[data.length] ;

			boolean flag=false;
			for(int index=0 ; index<data.length ; index++){
				if(flag){
					answer[index] = true ;
				}else{ //flag==0
					if(data[index] == true){
						flag=true ;
					}
					answer[index] = false ;
				}
			}
			return answer ;
		}

		// seed file to hash table
		public void seedToHash(String seed){
			String[] seed_list = seed.split("\t") ;

			for(String s : seed_list){
				String[] tmp = s.split(":") ;
				String node = tmp[0] ;
				boolean[] node_array = stringToArray(tmp[1]) ;	
				hash.put(node , node_array) ;
			}
		}

		// string to boolean array
		public boolean[] stringToArray(String s){
			String[] str = s.split(" ") ;

			boolean[] array = new boolean[str.length] ;

			for(int i=0 ; i<str.length ; i++){
				if( "1".equals(str[i]) )
					array[i] = true ;
				else
					array[i] = false ;
			}
			return array ;
		}
		public boolean[] getHash(String key){
			return (boolean[]) hash.get(key) ;
		}

		public void addHash(String key, boolean[] array){
			hash.put(key , array) ;
		}
		public void removeHash(String key) {
			hash.remove(key) ;
		}

		public int sumArray(boolean[] array){
			int sum=0 ;
			for(int i=0 ; i<array.length ; i++){
				if(array[i])
					sum+=1;
			}
			return sum ;
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
	}

	// Reducer 
	public static class Reduce extends MapReduceBase implements Reducer {
		private int T ;

		public void configure(JobConf job) {
			super.configure(job);
			T = job.getInt("T",0);
		}

		public void reduce(Object key, Iterator values, OutputCollector output, Reporter reporter) 
			throws IOException {
				int sum=0 ;
				while(values.hasNext()){
					IntWritable v = (IntWritable) values.next() ;
					sum += v.get();
					//		output.collect((Text)key, new IntWritable(v.get()) ) ;
				}
				if(sum>=T)
					output.collect((Text)key, new IntWritable(sum) );
			}
	}

	// Reducer for debug
	public static class Reduce_debug extends MapReduceBase implements Reducer {
		public void reduce(Object key, Iterator values, OutputCollector output, Reporter reporter) 
			throws IOException {
				output.collect((Text)key, new IntWritable(0) ) ;
			}
	}


	// Main
	public int run(String[] args) throws Exception {
		// Deal with argument
		String input_file = null ;
		String output_file = null ;
		int T = 0;
		int maxTransSize = 0 ;
		int mapper = 1 ;
		int reducer = 1 ;
		for (int i = 0; i < args.length; i++) {
			if ("-i".equals(args[i]))
				input_file = args[++i] ;
			if ("-o".equals(args[i]))
				output_file = args[++i] ;
			if ("-T".equals(args[i]))
				T = Integer.valueOf(args[++i]) ;
			if ("-maxTS".equals(args[i]))
				maxTransSize = Integer.valueOf(args[++i]) ;
			if ("-mapper".equals(args[i]))
				mapper = Integer.valueOf(args[++i]) ;
			if ("-reducer".equals(args[i]))
				reducer = Integer.valueOf(args[++i]) ;
		}


		// Configue
		JobConf conf = new JobConf(getConf(), SPAM.class);
		conf.setJobName("SPAM");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(input_file));
		FileOutputFormat.setOutputPath(conf, new Path(output_file));

		conf.setNumMapTasks(mapper);
		conf.setNumReduceTasks(reducer);
		
		// set parameters
		conf.setInt("T" , T) ;
		conf.setInt("maxTS" , maxTransSize) ;

		// delete output
		FileSystem dfs = FileSystem.get(conf);
		if (dfs.exists(new Path(output_file)))
			dfs.delete(new Path(output_file), true);

		// Run Map/Reduce
		long start = System.nanoTime();
		JobClient.runJob(conf);
		long duration = System.nanoTime() - start;
		System.err.println( "Job Time : "+ duration/1e+9 + " sec");

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new SPAM(), args);
		System.exit(res);
	}
}
