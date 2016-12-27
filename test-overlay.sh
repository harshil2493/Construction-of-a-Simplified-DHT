test_home=/s/chopin/b/grad/hkshah/CS455Final18Feb/ResubmittedTest
 
for i in `cat machine_list`
do
	echo 'logging into '${i}
	gnome-terminal -x bash -c "ssh -t ${i} 'cd $test_home; java cs455.overlay.node.MessagingNode 129.82.46.230 5000;bash;'" &
done
