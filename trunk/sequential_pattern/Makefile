target=SPAM_v2
input=n_cust1000_format_5.data
t=3
output=stdout
FLAG=-bitmap


java_path=/usr/local/java/bin

all:
	$(java_path)/javac -classpath .:./jdsl.jar $(target).java
run:
	$(java_path)/java -classpath .:./jdsl.jar $(target) $(FLAG) -i $(input) -t $(t) -tmp tmp > $(output) 
