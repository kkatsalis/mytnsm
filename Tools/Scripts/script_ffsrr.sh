
sim=1

x=0
y=30


echo "%%%%%%%%%%%%%%%%%%%%"
echo "BEGIN FirstFit SIMULATIONS"
echo "%%%%%%%%%%%%%%%%%%%%"

while [ $x -lt $y ]
do
java -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community127/cplex/bin/x86-64_linux -jar "test.jar" FirstFitSRR $sim $x wait
echo "simulation finished:  FirstFit sim:$z run:$x"
  x=$(( $x + 1 ))
done
echo
echo "%%%% All FirstFit SIMULATIONS FINISHED %%%%"