echo
echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%"
echo "BEGIN SIMULATIONS"
echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%"
echo
echo
echo "%%%%%%%%%%%%%%%%%%%%"
echo "BEGIN CLOUD SIMULATIONS"
echo "%%%%%%%%%%%%%%%%%%%%"
z=1
x=1
y=3
while [ $x -le $y ]
do
java -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community1263/cplex/bin/x86-64_linux -jar "CloudPaperController.jar" Lyapunov $z $x wait
echo "simulation finished: Lyapunov sim:$z run:$x"
  x=$(( $x + 1 ))
done
echo
echo "%%%% Lyapunov SIMULATIONS FINISHED %%%%"
echo 
echo
echo
echo
echo
echo
echo "%%%%%%%%%%%%%%%%%%%%"
echo "BEGIN FFRR SIMULATIONS"
echo "%%%%%%%%%%%%%%%%%%%%"
x=1
while [ $x -le $y ]
do
java -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community1263/cplex/bin/x86-64_linux -jar "CloudPaperController.jar" FFRR $z $x wait
echo "simulation finished:  FFRR sim:$z run:$x"
  x=$(( $x + 1 ))
done
echo
echo "%%%% FFRR SIMULATIONS FINISHED %%%%"
echo
echo
echo
echo "%%%%%%%%%%%%%%%%%%%%"
echo "BEGIN FFRR SIMULATIONS"
echo "%%%%%%%%%%%%%%%%%%%%"
x=1
while [ $x -le $y ]
do
java -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community1263/cplex/bin/x86-64_linux -jar "CloudPaperController.jar" FFRandom $z $x wait
echo "simulation finished:  FFRandom sim:$z run:$x"
  x=$(( $x + 1 ))
done
echo
echo "%%%% FFRandom SIMULATIONS FINISHED %%%%"
echo
echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%"
echo "ALL SIMULATIONS FINISHED"
echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%"
echo
echo
