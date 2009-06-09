# user configure
input=./sequential_pattern/dataset/max_gap_small_format.data
T=2
maxTS=6

# vm configue
java_path=/usr/bin
hadoop_path=/usr/local/hadoop-0.19.1
# gc3 config
#java_path=/usr/local/java  
#hadoop_path=/usr/local/hadoop
target=SPAM
fs_input=/dongogo/clustermap
fs_output=/dongogo/output

map:
	$(java_path)/java -classpath ./sequential_pattern:./sequential_pattern/jdsl.jar SPAM_v2 -bitmap -i $(input) -t 0 -tmp tmp > bitmap
	$(java_path)/javac ToClusterMap.java
	$(java_path)/java ToClusterMap bitmap > clustermap

hadoop:
	rm -rf $(target)_dir
	mkdir $(target)_dir
	$(java_path)/javac -classpath $(hadoop_path)/hadoop-0.19.1-core.jar -d $(target)_dir $(target).java
	$(java_path)/jar -cvf $(target).jar -C $(target)_dir .
	sudo $(hadoop_path)/bin/hadoop jar $(target).jar $(target) -i $(fs_input) -o $(fs_output) -T $(T) -maxTS $(maxTS) -mapper 4


rm:
	$(hadoop_path)/bin/hadoop fs -rm $(fs_input)
put:
	sudo $(hadoop_path)/bin/hadoop fs -put clustermap $(fs_input)
get:
	rm -rf output
	$(hadoop_path)/bin/hadoop fs -get $(fs_output) .

clean:
	rm -rf output
	rm -rf $(target)_dir
	rm -rf $(target).jar
	rm -rf bitmap
	rm -rf clustermap
