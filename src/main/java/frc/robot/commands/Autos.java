// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;
import java.util.ArrayList;
import java.util.Optional;

import com.choreo.lib.Choreo;
import com.choreo.lib.ChoreoTrajectory;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.Intake.SetIntake;
import frc.robot.commands.Pivot.SetPivot;
import frc.robot.commands.Shooter.SetShooterCommand;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.Indexer.IndexerStates;
import frc.robot.subsystems.Intake.IntakeStates;
import frc.robot.subsystems.Pivot.PivotState;
import frc.robot.subsystems.Shooter;

public final class Autos {  
  private static CommandSwerveDrivetrain s_Swerve = CommandSwerveDrivetrain.getInstance();
  private static Shooter s_Shooter = Shooter.getInstance();

  private static final PIDController thetaController = new PIDController(3, 1.4, 0); //tune?
  private static final PIDController xController = new PIDController(5, 1, 0);
  private static final PIDController yController = new PIDController(5, 1, 0);

  public static Command getAutoCommand(AutoPath autoPath){
    return autoPath.autoCommand;
  }
  public static Command FollowChoreoTrajectory(ChoreoTrajectory path) {
    SwerveRequest.ApplyChassisSpeeds drive = new SwerveRequest.ApplyChassisSpeeds();
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    ChoreoTrajectory traj = path;
    thetaController.reset();

    s_Swerve.setAutoStartPose(traj.getInitialPose());
    SmartDashboard.putNumber("Start pose x", traj.getInitialPose().getX());
    Command swerveCommand = Choreo.choreoSwerveCommand(
      traj,
        s_Swerve::getPose,
        xController,
        yController,                                                           
        thetaController,
        (ChassisSpeeds speeds) -> s_Swerve.setControl(drive.withSpeeds(speeds)),
        // () -> {return false;},
        // (ChassisSpeeds speeds) -> s_Swerve.applyRequest(() -> drive.withSpeeds(speeds)),
            () -> { Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
              return alliance.isPresent() && alliance.get() == Alliance.Red;},
              s_Swerve);
      
      return swerveCommand;
  }

  public static Command FourNoteSubwoofer(){
    ArrayList<ChoreoTrajectory> trajectory = Choreo.getTrajectoryGroup("FourPieceSubwoofer");
    return new SequentialCommandGroup(
    new InstantCommand(() -> {Pose2d initialPose;
    Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
    initialPose = alliance.isPresent() && alliance.get() != Alliance.Red ? trajectory.get(0).getInitialPose() : trajectory.get(0).flipped().getInitialPose();
    s_Swerve.resetOdo(initialPose);
    System.out.println(initialPose.getX() + " " + initialPose.getY());}),
    
    new ParallelCommandGroup(
      new SetPivot(PivotState.SUBWOOFER),
      new SetShooterCommand(1800)
    ),

    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.5),

    new ParallelCommandGroup(
        new SetShooterCommand(900),
        FollowChoreoTrajectory(trajectory.get(0)),
        new SetIndexer(IndexerStates.ON, true),
        new SetIntake(IntakeStates.ON)
      ),
      Commands.waitSeconds(0.3),

    new ParallelCommandGroup(
      new SetIntake(IntakeStates.OFF),
      FollowChoreoTrajectory(trajectory.get(1))
    ),
    
    new SetShooterCommand(1800),
    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.5),

    new ParallelCommandGroup(
      FollowChoreoTrajectory(trajectory.get(2)),
      new SetShooterCommand(900),
      new SetIndexer(IndexerStates.ON, true),
      new SetIntake(IntakeStates.ON)
    ),

    Commands.waitSeconds(0.3),
       
    new ParallelCommandGroup(
        FollowChoreoTrajectory(trajectory.get(3)),
        new SetIntake(IntakeStates.OFF)
      ),
    
    new SetShooterCommand(1800),
    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.5),
       
    new ParallelCommandGroup(
        FollowChoreoTrajectory(trajectory.get(4)),
        new SetShooterCommand(900),
        new SetIndexer(IndexerStates.ON, true),
        new SetIntake(IntakeStates.ON) 
      ),
      
    Commands.waitSeconds(0.3),
      
    new ParallelCommandGroup(
        FollowChoreoTrajectory(trajectory.get(5)),
        new SetIntake(IntakeStates.OFF)
      ),
    
    new SetShooterCommand(1800),
    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.5),
    new SetIndexer(IndexerStates.OFF, false),
    new SetShooterCommand(0)


      
       
      
      /*new ParallelCommandGroup(
        new SetPivot(PivotState.SUBWOOFER),
        new InstantCommand(() -> s_Shooter.setVelocity(2500))),
      Commands.waitSeconds(0.8),
      new SetIndexer(IndexerStates.ON, false),
      Commands.waitSeconds(0.8),
      new InstantCommand(() -> s_Shooter.setVelocity(0)),
      new SetIndexer(IndexerStates.OFF, false),

      new ParallelCommandGroup(
      new SetIntake(IntakeStates.ON),
      new SetIndexer(IndexerStates.ON, true),
      FollowChoreoTrajectory(trajectory.get(0))
      ),
      
      new ParallelCommandGroup(
        new SetIntake(IntakeStates.OFF),
        // new SetIndexer(IndexerStates.OFF, false), theoretically should not need this but we'll see
        FollowChoreoTrajectory(trajectory.get(1))
      ),

      new InstantCommand(() -> s_Shooter.setVelocity(2500)),
      Commands.waitSeconds(0.8),
      new SetIndexer(IndexerStates.ON, false),
      Commands.waitSeconds(0.8),
      new ParallelCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(0)), new SetIndexer(IndexerStates.OFF, false)),

      FollowChoreoTrajectory(trajectory.get(2)),

      new ParallelCommandGroup(
        new SetIntake(IntakeStates.ON),
        new SetIndexer(IndexerStates.ON, true),
        FollowChoreoTrajectory(trajectory.get(3))
      ),

      new ParallelCommandGroup(
        new SetIntake(IntakeStates.OFF),
        //new SetIndexer(IndexerStates.OFF),
        FollowChoreoTrajectory(trajectory.get(4))
      ),
      new InstantCommand(() -> s_Shooter.setVelocity(2500)),
      Commands.waitSeconds(0.8),
      new SetIndexer(IndexerStates.ON, false),
      Commands.waitSeconds(0.8),
      new ParallelCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(0)), new SetIndexer(IndexerStates.OFF, false)),

      FollowChoreoTrajectory(trajectory.get(5)),

      new ParallelCommandGroup(
        new SetIntake(IntakeStates.ON),
        new SetIndexer(IndexerStates.ON, true),
        FollowChoreoTrajectory(trajectory.get(6))
      ),

      new ParallelCommandGroup(
        new SetIntake(IntakeStates.OFF),
        //new SetIndexer(IndexerStates.OFF, IndexerMotors.BOTH),
        FollowChoreoTrajectory(trajectory.get(7))
      ),
      
      new InstantCommand(() -> s_Shooter.setVelocity(2500)),
      Commands.waitSeconds(0.8),
      new SetIndexer(IndexerStates.ON, false),
      Commands.waitSeconds(0.8),
      new ParallelCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(0)), new SetIndexer(IndexerStates.OFF, false))*/
    );
    
  }

  public static Command FourNoteCloseSide(){
    ArrayList<ChoreoTrajectory> trajectory = Choreo.getTrajectoryGroup("FourNoteCloseSide");
    return new SequentialCommandGroup(
    new SetPivot(PivotState.SUBWOOFER),
      new InstantCommand(() -> s_Shooter.setVelocity(2500)),
      Commands.waitSeconds(0.8),
      new SetIndexer(IndexerStates.ON, false),
      Commands.waitSeconds(0.8),
      new ParallelCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(0)), new SetIndexer(IndexerStates.OFF, false)),

    FollowChoreoTrajectory(trajectory.get(0)),

    new ParallelCommandGroup(
      new SetIntake(IntakeStates.ON),
      new SetIndexer(IndexerStates.ON, true),
      FollowChoreoTrajectory(trajectory.get(1))
    ),

    new SetIntake(IntakeStates.OFF),
    //new SetIndexer(IndexerStates.OFF, IndexerMotors.BOTH),
    // new SetShooterVelocity(2500),
    // Commands.waitSeconds(0.8),
    // new SetShooterVelocity(0),
        new InstantCommand(() -> s_Shooter.setVelocity(2500)),
    Commands.waitSeconds(0.8),
    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.8),
    new ParallelCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(0)), new SetIndexer(IndexerStates.OFF, false)),
    FollowChoreoTrajectory(trajectory.get(2))

    // new ParallelCommandGroup(
    // new 
    // )
    );
  }

  public static Command ThreeNoteFarSide(){
    ArrayList<ChoreoTrajectory> trajectory = Choreo.getTrajectoryGroup("ThreeNoteFarSide");
    return new SequentialCommandGroup(
    new InstantCommand(() -> {Pose2d initialPose;
      Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
      initialPose = alliance.isPresent() && alliance.get() != Alliance.Red ? trajectory.get(0).getInitialPose() : trajectory.get(0).flipped().getInitialPose();
      s_Swerve.resetOdo(initialPose);
      System.out.println(initialPose.getX() + " " + initialPose.getY());}),
     
    new ParallelCommandGroup(
      new SetPivot(PivotState.SUBWOOFER),
      new SetShooterCommand(1800)
    ),
    
    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.5),

    new ParallelCommandGroup(
      FollowChoreoTrajectory(trajectory.get(0)),
      new SetShooterCommand(900),
      new SetIndexer(IndexerStates.ON, true),
      new SetIntake(IntakeStates.ON)
    ),
      
    Commands.waitSeconds(0.3),

    new ParallelCommandGroup(
      FollowChoreoTrajectory(trajectory.get(1)),
      new SetIntake(IntakeStates.OFF)
    ),

    new ParallelCommandGroup(
      new SetPivot(20), //this angle is gonna be totally arbitrary, we are going to need to test and find the right angle
      new SetShooterCommand(1800)
    ),

    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.5),

    new ParallelCommandGroup(
      FollowChoreoTrajectory(trajectory.get(2)),
      new SetPivot(PivotState.SUBWOOFER),
      new SetShooterCommand(900),
      new SetIndexer(IndexerStates.ON, true),
      new SetIntake(IntakeStates.ON)
    ),
    
    Commands.waitSeconds(0.3),

    new ParallelCommandGroup(
      FollowChoreoTrajectory(trajectory.get(3)),
      new SetIntake(IntakeStates.OFF)
    ),

    new ParallelCommandGroup(
      new SetPivot(20), //also arbitrary, but should be same angle as the other non-subwoofer shot in this path
      new SetShooterCommand(1800)
    ),

    new SetIndexer(IndexerStates.ON, false),
    Commands.waitSeconds(0.5),
    new SetIndexer(IndexerStates.OFF, false),
    new SetShooterCommand(0)
    );
  }

  public static Command TwoNote() {
    ArrayList<ChoreoTrajectory> trajectory = Choreo.getTrajectoryGroup("TwoNote");

    // Pose2d initialPose;
    // Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
    // initialPose = alliance.isPresent() && alliance.get() == Alliance.Red ? trajectory.get(0).getInitialPose() : trajectory.get(0).flipped().getInitialPose();
    // s_Swerve.resetOdo(initialPose);
    // System.out.println(initialPose.getX() + " " + initialPose.getY());

    return new SequentialCommandGroup(
      new InstantCommand(() -> {Pose2d initialPose;
    Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
    initialPose = alliance.isPresent() && alliance.get() != Alliance.Red ? trajectory.get(0).getInitialPose() : trajectory.get(0).flipped().getInitialPose();
    s_Swerve.resetOdo(initialPose);
    System.out.println(initialPose.getX() + " " + initialPose.getY());}),
      new ParallelCommandGroup(new SetPivot(PivotState.SUBWOOFER),
      new InstantCommand(() -> s_Shooter.setVelocity(1800))),
      Commands.waitSeconds(0.8),
      new SetIndexer(IndexerStates.ON, false),
      Commands.waitSeconds(0.8),

      new ParallelCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(0)), new SetIndexer(IndexerStates.OFF, false)),

      //new ParallelCommandGroup(FollowChoreoTrajectory(trajectory.get(0)), new SetIntake(IntakeStates.ON), new SetIndexer(IndexerStates.ON, true)),
      new ParallelCommandGroup(FollowChoreoTrajectory(trajectory.get(0)), new SetIndexer(IndexerStates.ON, false)),

      //new ParallelCommandGroup(FollowChoreoTrajectory(trajectory.get(1)), new SetIntake(IntakeStates.OFF), new SetIndexer(IndexerStates.OFF, false)),

      new ParallelCommandGroup(FollowChoreoTrajectory(trajectory.get(1)), new SetIndexer(IndexerStates.OFF, false))

      // shootSequence()
      );
  }

  private static Command shootSequence() {

    return new SequentialCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(1800)),
      Commands.waitSeconds(0.8),
      new SetIndexer(IndexerStates.ON, false),
      Commands.waitSeconds(0.8),
      new ParallelCommandGroup(new InstantCommand(() -> s_Shooter.setVelocity(0)), new SetIndexer(IndexerStates.OFF, false))
      );
      
  }

  public static Command Horizontal() {
     ArrayList<ChoreoTrajectory> trajectory = Choreo.getTrajectoryGroup("Horizontal Test");
    
     return new SequentialCommandGroup(
      new InstantCommand(() -> {Pose2d initialPose;
    Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
    initialPose = alliance.isPresent() && alliance.get() != Alliance.Red ? trajectory.get(0).getInitialPose() : trajectory.get(0).flipped().getInitialPose();
    s_Swerve.resetOdo(initialPose);
    System.out.println(initialPose.getX() + " " + initialPose.getY());}),
      FollowChoreoTrajectory(trajectory.get(0)),
      Commands.waitSeconds(0.3),
      FollowChoreoTrajectory(trajectory.get(1))
     );
  }

  public static Command Straight() {
    ArrayList<ChoreoTrajectory> trajectory = Choreo.getTrajectoryGroup("Straight");
    
     return new SequentialCommandGroup(
      new InstantCommand(() -> {Pose2d initialPose;
    Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
    initialPose = alliance.isPresent() && alliance.get() != Alliance.Red ? trajectory.get(0).getInitialPose() : trajectory.get(0).flipped().getInitialPose();
    s_Swerve.resetOdo(initialPose);
    System.out.println(initialPose.getX() + " " + initialPose.getY());}),
      FollowChoreoTrajectory(trajectory.get(0)),
      Commands.waitSeconds(0.3),
      FollowChoreoTrajectory(trajectory.get(1))
     );
  }

  public static Command Rotation() {
    ArrayList<ChoreoTrajectory> trajectory = Choreo.getTrajectoryGroup("Rotation");
    
     return new SequentialCommandGroup(
      new InstantCommand(() -> {Pose2d initialPose;
    Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
    initialPose = alliance.isPresent() && alliance.get() != Alliance.Red ? trajectory.get(0).getInitialPose() : trajectory.get(0).flipped().getInitialPose();
    s_Swerve.resetOdo(initialPose);
    System.out.println(initialPose.getX() + " " + initialPose.getY());}),
      FollowChoreoTrajectory(trajectory.get(0))
     );
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
      ThreeNoteFarSide("ThreeNoteFarSide", ThreeNoteFarSide()),
      FourNoteCloseSide("FourNoteCloseSide", FourNoteCloseSide()),
      FourNoteSubwoofer("FourNoteSubwoofer", FourNoteSubwoofer()),
      TwoNoteSubwoofer("TwoNoteSuboofer", TwoNote()),
      Horizontal("Horizontal", Horizontal()),
      Straight ("Straight", Straight()),
      Rotation ("Rotation", Rotation());
    

      String name;
      Command autoCommand;

      private AutoPath(String a, Command autoCommand){
        name = a;
        this.autoCommand = autoCommand;
      }
  }
}