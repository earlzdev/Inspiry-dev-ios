package app.inspiry

import android.content.Context

class FlipperInitializerImpl: FlipperInitializer {
    override fun initialize(appContext: Context) {
        //it is not used anywhere
        /*
        if (FlipperUtils.shouldEnableFlipper(appContext)) {

            try {
                SoLoader.init(appContext, false)

                val client: FlipperClient = AndroidFlipperClient.getInstance(appContext)
                client.addPlugin(InspectorFlipperPlugin(appContext, DescriptorMapping.withDefaults()))
                client.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }*/
    }
}