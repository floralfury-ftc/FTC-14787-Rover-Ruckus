package org.firstinspires.ftc.team14787;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

/** Mineral location enumerable */
enum MineralLocation {
    LEFT, CENTER, RIGHT
}

/**
 * Robot vision representation, encloses methods relating to Vuforia image capturing
 * and TensorFlow Object Detection
 */
class Vision {

    /** Instance information */
    private boolean tfodEnabled;

    /** TFOD configuration and label */
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    /** Vuforia API Key */
    private static final String VUFORIA_KEY = "AW3RM/7/////AAABmdYUnHQNFUBUtLcj9yNEiU1eT9NilwrFUzWl2FV1fFXafePbmAt1mX9m1x5ZSvHHFXCHKPWLGD2w/X14S4Zie69lPlJzVvT1JE+SJCgDiNabghLZdKai9ITjNLnRliOfaGcGF/sEgr7AP/oZkc4nWetITL3wve+hclRlqcvEJRvixMgBrCluh8F0L58pKEZT6MUVtXes4lEx5agdsWOvgTkLYzxqaSHx8f+sO/qXgAojxRzEdcJ5o2MdgpN9YuZJoSSSvpT1yvPpp5OuVhdH5CpkNNnP0qVHdnvH5QK7g5TC2bKeTvx60DRroVa5/U4ZrMyQqsRnz78gagsFsXqKa+xf1HS324Y5zoUZ/a2UFlfx";

    /** Vuroria and TFOD instances */
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;

    /** Reference to the encapsulating robot's hardware configuration */
    private HardwareMap hardwareMap;

    /*
    /** Telemetry instance to provide debugging information /
    Telemetry telemetry;
    */

    /**
     * Create a camera management interface
     * @param hardwareMap Reference to the encapsulating robot's hardware configuration
     */
    Vision(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;

        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        initVuforia();

        // Initialize TensorFlow object detection
        initTfod();
        enableDetection();
    }

    /** Enable TFOD */
    void enableDetection() {
        // Sampling with TFOD
        // Activate Tensor Flow Object Detection.
        tfod.activate();
        tfodEnabled = true;
    }

    /** Disable TFOD */
    void disableDetection() {
        if (tfodEnabled) tfod.deactivate();
        tfodEnabled = false;
    }

    /**
     * Retrieve initial gold location using the positions of the two,
     * leftmost minerals
     * @return The detected relative location of the gold mineral
     */
    MineralLocation getGoldLocation() {
        MineralLocation goldLocation = null;

        if (tfod != null) {
            List<Recognition> updatedRecognitions = getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                // Only run if there are two detected game objects
                if (updatedRecognitions.size() == 2) {
                    // Retrieve instances of both unordered objects
                    Recognition mineral1 = updatedRecognitions.get(0);
                    Recognition mineral2 = updatedRecognitions.get(1);

                    // Determine left and center minerals
                    Recognition leftMineral = mineral1.getLeft() < mineral2.getLeft() ? mineral1 : mineral2;
                    Recognition centerMineral = mineral1.getLeft() > mineral2.getLeft() ? mineral1 : mineral2;

                    // Determine gold location
                    if (leftMineral.getLabel().equals(LABEL_GOLD_MINERAL)) {
                        goldLocation = MineralLocation.LEFT;
                    } else if (centerMineral.getLabel().equals(LABEL_GOLD_MINERAL)) {
                        goldLocation = MineralLocation.CENTER;
                    } else {
                        goldLocation = MineralLocation.RIGHT;
                    }

                    // Provide debugging information
                    /*
                    telemetry.addData("Base Mineral Info", "\nMineral1 (%s): getLeft() : %f\nMineral2 (%s): getLeft() : %f", mineral1.getLabel(), mineral1.getLeft(), mineral2.getLabel(), mineral2.getLeft());
                    telemetry.addData("Mineral Info", "\nLeft Mineral (%s): getLeft() : %f\nRight Mineral (%s): getLeft() : %f", leftMineral.getLabel(), leftMineral.getLeft(), centerMineral.getLabel(), centerMineral.getLeft());
                    telemetry.addData("Gold Location Info", "\ngoldLocation: %s", goldLocation.toString());
                    telemetry.update();
                    */
                }
            }
        }
        return goldLocation;
    }

    /**
     * TFOD wrapper method
     * @return Currently detected game objects
     */
    List<Recognition> getUpdatedRecognitions() {
        return tfod.getUpdatedRecognitions();
    }

    /**
     * Initialize the Vuforia localization engine
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine
    }

    /**
     * Initialize the Tensor Flow Object Detection engine
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}
