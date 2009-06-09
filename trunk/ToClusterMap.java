import java.io.* ;

class ToClusterMap{
	int powk ;
	int cluster ;

	public void go(String input){
		try{
			BufferedReader buf = new BufferedReader( new FileReader(input) ) ; 
			String s = buf.readLine() ;
			String[] tmps = s.split(" ");
			cluster = Integer.valueOf(tmps[0]);
			powk = Integer.valueOf(tmps[1]) ;					

//			System.out.println("powk:"+powk+" clsuter:"+cluster) ;

			buf.close() ;
			//for each cluster
			for(int i=0 ; i<cluster ; i++){
				System.err.println("cluster:"+i) ;
				BufferedReader b = new BufferedReader( new FileReader(input) ) ; 
				String t = b.readLine() ;

				while( (t=b.readLine())!=null ){
					String[] tmp = t.split("\t") ;
					String node = tmp[0] ;
					String array_string = tmp[1] ;	

					System.out.print(node) ;
					System.out.print(":") ;

					String[] array = array_string.split(" ") ;
	
					int base = i*powk ;
					for(int k=0 ; k<powk ; k++){
						int index = base+k ;
						System.out.print(array[index]+" ") ;
					}
					System.out.print("\t") ;	
				}
				b.close() ;
				System.out.print("\n") ;
			}

		}	
		catch(IOException e){
			e.printStackTrace() ;
		}
			

	}

	public static void main(String[] args){
		ToClusterMap c = new ToClusterMap();
		c.go(args[0]) ;
	}	


}
