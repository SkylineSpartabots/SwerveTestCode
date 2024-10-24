// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Drivetrain;

import org.photonvision.targeting.PhotonTrackedTarget;

import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.subsystems.CommandSwerveDrivetrain.DriveControlSystems;
import frc.robot.subsystems.CommandSwerveDrivetrain.Drivetrain;
import frc.robot.RobotContainer;

public class HoldHeading extends Command {

    private DriveControlSystems controlSystem  = new DriveControlSystems();

    public HoldHeading() {
    }

    @Override
    public void initialize() {
      controlSystem.setLastHeading();
      controlSystem.toggleHeadingControl();
      SmartDashboard.putBoolean("headingON", true);
    }
    
    @Override
    public void execute(){
    }

    @Override
    public void end(boolean interrupted) {
       controlSystem.toggleHeadingControl(); 
       SmartDashboard.putBoolean("headingON", false);
    }

    @Override
    public boolean isFinished() { //always use in WhileTrue
        return false;
    }
}
