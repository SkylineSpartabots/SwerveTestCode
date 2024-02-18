// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Indexer extends SubsystemBase {

    private static Indexer instance;

    public static Indexer getInstance() {
        if (instance == null) {
            instance = new Indexer();
        }
        return instance;
    }

    private String state;
    private double currentTopSpeed = 0;
    private double currentBottomSpeed = 0;
    private CANSparkMax indexerTopM;
    private CANSparkMax indexerBottomM;

    public Indexer() {
        indexerTopM = new CANSparkMax(Constants.HardwarePorts.indexerM, MotorType.kBrushless);
        indexerBottomM = new CANSparkMax(Constants.HardwarePorts.indexerM, MotorType.kBrushless);
        indexerBottomM.setInverted(true);
    }

    public enum IndexerStates {
        ON(1),
        OFF(0);
        private double speed;
        public double getValue() {
            return speed;
        } // how use

        IndexerStates(double    speed) {
            this.speed = speed;
        }
    }

    /**
     * @param MotorLocation
     * true = top motor
     * false = bottom motor
     */
    public void setSpeed(double speed, boolean MotorLocation) {
        if(MotorLocation) {
            indexerTopM.set(speed);
            currentTopSpeed = speed;
        } else if (MotorLocation) {
            indexerBottomM.set(speed);
            currentBottomSpeed = speed;
        }
    }

    public double getSpeed(boolean MotorLocation) { //gets specific Speed (i hope)
        if(MotorLocation) {return currentTopSpeed;} else{ return currentBottomSpeed;}
    }

    @Override
    public void periodic() {
    }

    @Override
    public void simulationPeriodic() {
    }
}
