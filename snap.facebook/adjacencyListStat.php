<?php

	if ($argc < 2) {
		echo "You must insert a dataset name\n";
		die();
	}

	for ($file=1; $file<$argc; $file++) {

		$DATASET = fopen(getcwd()."/".$argv[$file].".edges", "r") or die("bad dataset file selected");
		$TARGET = fopen(getcwd()."/".$argv[$file].".adjacencyStat", "w+") or die("could not open new dataset");

		$MATRIX;
		$MAX_VALUE = 0;

		while (($line = fgets($DATASET)) !== false ) {
			$edge = explode(" ", $line);		
		
			$MATRIX[intval($edge[0])][intval($edge[1])] = 1;

			$MAX_VALUE = max($MAX_VALUE,intval($edge[0]),intval($edge[1]));
		}
		if (!feof($DATASET)) {
			echo "Error: unexpected fgets() fail\n";
			die();
		} else {
			fclose($DATASET);
		}

		$contacts = Array();
		for ($i=0; $i<=$MAX_VALUE; $i++) {
			if ( (gettype($MATRIX[$i]) != "NULL") && (in_array(1, $MATRIX[$i])) ) {
				$contacts[count($contacts)] = array_sum($MATRIX[$i]);
			}
		}

		$average = array_sum($contacts) / count($contacts);

		fwrite($TARGET, "numero utenti: ".count($contacts)."\nmedia contatti: $average");
	
		fclose($TARGET);
	}
?>
