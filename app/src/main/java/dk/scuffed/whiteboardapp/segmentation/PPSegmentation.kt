package dk.scuffed.whiteboardapp.segmentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import com.baidu.paddle.fastdeploy.LitePowerMode
import com.baidu.paddle.fastdeploy.RuntimeOption
import com.baidu.paddle.fastdeploy.vision.SegmentationResult
import com.baidu.paddle.fastdeploy.vision.Visualize
import com.baidu.paddle.fastdeploy.vision.segmentation.PaddleSegModel

class PPSegmentation (context : Context, model : Model) {
    enum class Model(val modelName : String, val width : Int, val height : Int) {
        PORTRAIT("humansegv2portrait", 256, 144),
        GENERALLITE("humansegv2generallite", 192, 192),
        GENERALMOBILE("humansegv2generalmobile", 192, 192)
    }

    val chosenModel : Model
    val segmentationModel : PaddleSegModel = PaddleSegModel()
    val segmentationResult : SegmentationResult = SegmentationResult()
    public fun Segment(input : Bitmap) : Bitmap{
        // predict on the downscaled input
        segmentationModel.predict(input.scale(chosenModel.width, chosenModel.height), segmentationResult)

        // create and return a bitmap with result from prediction
        var bitmapResult : Bitmap = Bitmap.createBitmap(chosenModel.width, chosenModel.height, Bitmap.Config.ARGB_8888)
        Visualize.visSegmentation(bitmapResult, segmentationResult)
        return bitmapResult
    }

    init {
        chosenModel = model
        // get all path names
        val modelDir = "models/${model.modelName}";
        val modelCacheDir = context.cacheDir.toString() + "/" + modelDir;
        val modelFile = "$modelCacheDir/model.pdmodel"
        val paramsFile = "$modelCacheDir/model.pdiparams"
        val configFile = "$modelCacheDir/deploy.yaml"

        // copy the model from assets into the cache
        Utils.copyDirectoryFromAssets(context, modelDir, modelCacheDir)

        // configure how the model is run
        val option = RuntimeOption()
        option.mCpuThreadNum = 1
        option.mLitePowerMode = LitePowerMode.LITE_POWER_HIGH
        option.mEnableLiteFp16 = true

        // configure and initialize the model
        segmentationModel.setVerticalScreenFlag(true)
        segmentationModel.init(modelFile, paramsFile, configFile, option)

        // set up segmentation result to use as buffer
        segmentationResult.mEnableCxxBuffer = true

    }

}