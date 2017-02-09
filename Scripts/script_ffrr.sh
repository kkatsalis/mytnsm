z=2
x=1
y=100
echo "%%%%%%%%%%%%%%%%%%%%"
echo "BEGIN FFRR SIMULATIONS"
echo "%%%%%%%%%%%%%%%%%%%%"
while [ $x -le $y ]
do
java -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community1263/cplex/bin/x86-64_linux -jar "CloudPaperController.jar" FFRR $z $x wait
echo "simulation finished:  FFRR sim:$z run:$x"
  x=$(( $x + 1 ))
done
echo
echo "%%%% ALL FFRR SIMULATIONS FINISHED %%%%"
