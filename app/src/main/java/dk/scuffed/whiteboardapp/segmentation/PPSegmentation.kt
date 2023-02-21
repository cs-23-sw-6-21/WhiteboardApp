package dk.scuffed.whiteboardapp.segmentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.baidu.paddle.fastdeploy.LitePowerMode
import com.baidu.paddle.fastdeploy.RuntimeOption
import com.baidu.paddle.fastdeploy.vision.Visualize
import com.baidu.paddle.fastdeploy.vision.segmentation.PaddleSegModel

class PPSegmentation (context : Context, model : Model) {
    enum class Model(val modelName : String) {
        PORTRAIT("humansegv2portrait"),
        GENERALLITE("humansegv2generallite"),
        GENERALMOBILE("humansegv2generalmobile")
    }

    val segmentationModel : PaddleSegModel = PaddleSegModel()

    public fun Segment(input : Bitmap) : Bitmap?{
        // TEMPORARY HACK TO TEST
        val result = segmentationModel.predict(input)
        var bitmapResult : Bitmap = input!!
        Visualize.visSegmentation(bitmapResult, result, 1.0f)

        Log.d("sdflksjfklsd", "COlor at corner: " + bitmapResult.getColor(0,0))
        Log.d("sdhfsd","Segment sum: " + 1)
        return null
    }

    init {
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
    }

}