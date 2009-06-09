#!/usr/bin/perl
#
#
use strict ;

my %hash ;

while(<>){
	my $line = $_ ;

	$line =~ s/  /\t/g ;

	$line =~ /(.*?)\t(.*?)\t(.*)/ ;
	
	$hash{$2} .= $1."\t".$3."\n" ;
}

foreach my $key (sort { $a <=> $b } keys %hash){
	my $cid = $key-1 ;
	my $value = $hash{$key} ;

	my %tid ;
	while( $value =~ /(.*?)\n/s ){
		my $tmp = $1 ;
		$value = $';
		
		$tmp =~ /(.*?)\t(.*)/ ;
		$tid{$1}=$2 ;
	}

	foreach my $key( sort {$a<=>$b} keys %tid){
		print $cid."\t".$key."\t".$tid{$key} ;
		print "\n" ;	
	}
}

