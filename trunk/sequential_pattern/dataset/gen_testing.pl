#!/usr/bin/perl
#

use strict ;


my $itemset_size = 10 ;
my $num_cluter = 5 ;
my $max_trans = 2 ;



my $i ;
for($i=0 ; $i<$num_cluter ; $i++){
	my $id = $i ;
	# print trans
	my $k ;
	for($k=0 ; $k<$max_trans ; $k++){
		print "$i\t0\t" ;

		# print item set
		my $j ;
		for($j=0 ;$j<$itemset_size ; $j++){
			print "," if ($j!=0) ;
			print "$j" ;
		}
		
		print "\n" ;
	}
}
