// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;
import java.util.ArrayList;
import java.util.Optional;

import com.choreo.lib.*;
import com.ctre.phoenix6.mechanisms.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.subsystems.CommandSwerveDrivetrain;

public final class Autos {  
      
  ChoreoTrajectory traj;
  private static CommandSwerveDrivetrain s_Swerve = CommandSwerveDrivetrain.getInstance();

  // Return auto selected in Shuffleboard
  /**
   * Runs an auto command depending on the AutoType enum variable. Works by assembling all commands
   * that will be executed in the autopath into one SequentialCommandGroup and then scheduling that 
   * command group to the command scheduler. 
   * @param auto AutoType enum representing the auto path that is to be run. 
   */
  public static Command getAutoCommand(AutoPath auto) {

    ArrayList<ChoreoTrajectory> traj = new ArrayList<>();
    traj.add(Choreo.getTrajectory(auto.name));
    //ArrayList<ChoreoTrajectory> traj = Choreo.getTrajectoryGroup(auto.name);

    PIDController xController = new PIDController(1, 0, 0); //TODO: tune
    PIDController yController = new PIDController(1, 0, 0);
    PIDController thetaController = new PIDController(1, 0, 0);
    SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
      .withDeadband(Constants.MaxSpeed * 0.1).withRotationalDeadband(Constants.MaxAngularRate * 0.1) // Add a 10% deadband
      .withDriveRequestType(DriveRequestType.OpenLoopVoltage);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    ArrayList<Command> commandsToSchedule = new ArrayList<Command>();

    s_Swerve.resetOdo(traj.get(0).getInitialPose());
    for(int i = 0; i < traj.size(); i++){
      Command swerveCommand = Choreo.choreoSwerveCommand(
      traj.get(i), 
      s_Swerve::getPose, 
      xController,
      yController,
      thetaController,
      (ChassisSpeeds speeds) -> s_Swerve.applyRequest(() -> drive.withVelocityX(speeds.vxMetersPerSecond).withVelocityY(speeds.vyMetersPerSecond).withRotationalRate(speeds.omegaRadiansPerSecond)),
      () -> { Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
        return alliance.isPresent() && alliance.get() == Alliance.Red; }, //decides whether or not the math should be mirrored (depends on alliance)
      s_Swerve);

      if(auto.parallelToPath[i]){
        ParallelCommandGroup curr = new ParallelCommandGroup();
        curr.addCommands(auto.mechCommands[i]);
        curr.addCommands(swerveCommand);
        commandsToSchedule.add(curr);
      } else {
        commandsToSchedule.add(swerveCommand);
        commandsToSchedule.add(auto.mechCommands[i]);
      }
    }
    SequentialCommandGroup group = new SequentialCommandGroup();
    for (Command i : commandsToSchedule) {
      group.addCommands(i);
    }

    return group;
      // return swerveCommand;
  }

  /*
   * Enum for the different autos. Contains a name and a mechCommands array. The mechCommands array contains 
   * all the commands that the mechanisms will use (stuff that is unrelated to the drivetrain). These commands 
   * will be executed in the order they are in the array during the auto path. Refer to runAutoCommand(AutoType auto).
   */
  public enum AutoPath {

    //when writing enums, if you want multiple mechCommands to run before the next path, put them in a sequential command group
    //if you want those mechCommands to run in parallel, put them in a parallelCommandGroup
    //if you want to run a mechCommand or mechCommandGroup in parallel with a path, create a boolean array with true values corresponding to the mechCommands you want to run in parallel.
      StraightPathTesting("StraightPathTesting", new Command[]{}),
      AngledDrivingTesting("AngledDrivingTesting", new Command[]{}),
      StraightAndTurn180Testing("StraightAndTurn180Testing", new Command[]{}),
      TESTPATH("TestPath", new Command[]{new InstantCommand()});

      String name;
      Command[] mechCommands;
      boolean[] parallelToPath;

      private AutoPath(String a, Command[] mechCommands, boolean[] parallelToPath){
        name = a;
        this.mechCommands = mechCommands;
        this.parallelToPath = parallelToPath;
        
      }

      private AutoPath(String a, Command[] mechCommands){
        name = a;
        this.mechCommands = mechCommands;
        parallelToPath = new boolean[mechCommands.length];
      }
  }
}