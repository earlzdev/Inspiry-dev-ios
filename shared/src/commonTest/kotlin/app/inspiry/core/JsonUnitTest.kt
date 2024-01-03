package app.inspiry.core

import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.helper.JsonHelper
import app.inspiry.core.serialization.*
import app.inspiry.core.media.ProgramCreator
import app.inspiry.core.media.Template
import app.inspiry.palette.model.TemplatePalette
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test

class JsonUnitTest {

    val json = JsonHelper.initJson()

    @Test
    fun testInterpolator_1() {
        val testJson = "elegantlySlowOut"
        val interpolator = json.decodeFromString(InterpolatorSerializer, testJson)
        print(interpolator::class)
    }

    @Test
    fun testInterpolator_2() {
        val testJson =
            """
                {"type":"spring","amplitude":0.12,"frequency":20}
            """
        val interpolator = json.decodeFromString(InspInterpolator.serializer(), testJson)
        print(interpolator::class)
    }

    @Test
    fun testAnimator() {
        val testJson = "{\n" +
                "            \"type\": \"move_to_y\",\n" +
                "            \"from\": 1,\n" +
                "            \"to\": 0,\n" +
                "            \"durationMillis\": 2000,\n" +
                "            \"startTimeMillis\": 1000,\n" +
                "            \"interpolator\": \"elegantlySlowOut\"\n" +
                "          }\n"

        val animator = json.decodeFromString(AnimatorSerializer, testJson)

        println(animator)

        val backToJson = json.encodeToString(AnimatorSerializer, animator)

        println(backToJson)
    }

    @Test
    fun testPalette() {
        val testJson = "{\n" +
                "    \"choices\": [\n" +
                "      {\n" +
                "        \"color\": \"#DFC9BD\",\n" +
                "        \"elements\": [\n" +
                "          {\n" +
                "            \"type\": \"elementBackgroundColor\",\n" +
                "            \"id\": \"pathMask\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"type\": \"image\",\n" +
                "            \"id\": \"imageMask\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"elements\": [\n" +
                "          {\n" +
                "            \"type\": \"textColor\",\n" +
                "            \"id\": \"textLinkBottom\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"type\": \"textColor\",\n" +
                "            \"id\": \"textTitle\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"type\": \"textColor\",\n" +
                "            \"id\": \"insideText\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"elements\": [\n" +
                "          {\n" +
                "            \"type\": \"background\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"type\": \"elementBackgroundColor\",\n" +
                "            \"id\": \"insideText\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }"

        val palette = json.decodeFromString<TemplatePalette>(testJson)

        println(palette)

        val backToJson = json.encodeToString(palette)

        println(backToJson)
    }

    @Test
    fun testLayoutPosition() {

        val testJson = "{" +
                "\"width\": \"1w\",\n" +
                "      \"height\": \"449/1920h\",\n" +
                "      \"anchorY\": \"bottom\",\n" +
                "      \"anchorX\": \"center\",\n" +
                "      \"y\": \"0.25h\"" +
                "}"

        val layoutPosition = json.decodeFromString(LayoutPositionSerializer, testJson)

        println(layoutPosition)

        val backToJson = json.encodeToString(LayoutPositionSerializer, layoutPosition)

        println(backToJson)
    }

    @Test
    fun testLayoutPosition2() {

        val testJson = "{" +
                "\"width\": \"1w\",\n" +
                "      \"height\": \"449/1920h\",\n" +
                "      \"y\": \"0.25h\"" +
                "}"

        val layoutPosition = json.decodeFromString(LayoutPositionSerializer, testJson)

        println(layoutPosition)

        val backToJson = json.encodeToString(LayoutPositionSerializer, layoutPosition)

        println(backToJson)
    }

    @Test
    fun testAnimParam() {

        val testJson = "{\n" +
                "        \"wordInterpolator\": {\n" +
                "          \"type\": \"accelerate\",\n" +
                "          \"factor\": 1.2\n" +
                "        },\n" +
                "        \"textAnimators\": [\n" +
                "          {\n" +
                "            \"duration\": 8,\n" +
                "            \"interpolator\": \"flatIn25expOut\",\n" +
                "            \"type\": \"fade\",\n" +
                "            \"from\": 0,\n" +
                "            \"to\": 1\n" +
                "          },\n" +
                "          {\n" +
                "            \"duration\": 8,\n" +
                "            \"interpolator\": \"flatIn25expOut\",\n" +
                "            \"type\": \"move\",\n" +
                "            \"fromX\": 0,\n" +
                "            \"toX\": 0,\n" +
                "            \"fromY\": 1,\n" +
                "            \"toY\": 0,\n" +
                "            \"relativeToParent\": true\n" +
                "          }\n" +
                "        ],\n" +
                "        \"charDelay\": 2,\n" +
                "        \"wordDelayMillis\": 150,\n" +
                "        \"lineDelayMillis\": 150\n" +
                "      }\n"

        val obj = json.decodeFromString(TextAnimationParamsSerializer, testJson)

        println(obj)

        val backToJson = json.encodeToString(TextAnimationParamsSerializer, obj)

        println(backToJson)
    }

    @Test
    fun testMediaText() {

        val testJson = """
            {
              "anchorY": "center",
              "anchorX": "center",
              "width": "wrap_content",
              "height": "wrap_content",
              "textColor": "#ffffff",
              "forPremium": true,
              "type": "text",
              "text": "A caption\ndisplayed\nline by line",
              "innerGravity": "left",
              "textSize": "2/23w",
              "font": {
                "fontStyle": "bold",
                "fontPath": "montserrat"
              },
              "backgroundGradient": {
                "colors": [
                  "#001FFF",
                  "#D90FFF"
                ]
              },
              "backgroundMarginTop": -0.55,
              "backgroundMarginBottom": -0.05,
              "backgroundMarginLeft": 0.2,
              "backgroundMarginRight": 0.2,
              "animationParamIn": {
                "lineDelayMillis": 200,
                "textAnimators": [
                  {
                    "type": "move_to_x",
                    "durationMillis": 350,
                    "from": 1,
                    "to": 0,
                    "interpolator": "elegantlySlowOut"
                  }
                ]
              },
              "animatorsOut": [
                {
                  "type": "fade",
                  "duration": 9,
                  "from": 1,
                  "to": 0
                }
              ]
            }
        """.trimIndent()

        val obj = json.decodeFromString(MediaSerializer, testJson)

        println(obj)

        val backToJson = json.encodeToString(MediaSerializer, obj)

        println(backToJson)
    }

    @Test
    fun testAnimParamWithGroups() {

        val testJson = """{
    "lineDelayMillis": 200,
    "textAnimators": [
      {
        "type": "move_to_x",
        "durationMillis": 350,
        "from": 1,
        "to": 0,
        "interpolator": "elegantlySlowOut"
      }
    ],
    "backgroundAnimatorGroups": [
      {
        "group": "uneven_not_first",
        "animators": [
          {
            "type": "fade",
            "duration": 0,
            "from": 0,
            "to": 0
          }
        ]
      },
      {
        "group": "even_or_first",
        "animators": [
          {
            "type": "clip",
            "startTimeMillis": 150,
            "duration": 30,
            "direction": "right_to_left",
            "interpolator": "elegantlySlowOut"
          }
        ]
      }
    ]
  } """

        val obj = json.decodeFromString(TextAnimationParamsSerializer, testJson)

        println(obj)

        val backToJson = json.encodeToString(TextAnimationParamsSerializer, obj)

        println(backToJson)
    }

    @Test
    fun testMediaPath() {

        val testJson = "{\n" +
                "      \"type\": \"path\",\n" +
                "      \"width\": \"461/540w\",\n" +
                "      \"height\": \"273/320h\",\n" +
                "      \"color\": \"#CCffffff\",\n" +
                "      \"anchorX\": \"center\",\n" +
                "      \"anchorY\": \"center\",\n" +
                "      \"paintStyle\": \"STROKE\",\n" +
                "      \"strokeWidth\": \"0.03m\",\n" +
                "      \"id\": \"path\",\n" +
                "      \"animatorsIn\": [\n" +
                "        {\n" +
                "          \"type\": \"fade\",\n" +
                "          \"duration\": 0,\n" +
                "          \"from\": 0.8,\n" +
                "          \"to\": 0.8\n" +
                "        }\n" +
                "      ],\n" +
                "      \"movements\": [\n" +
                "        {\n" +
                "          \"type\": \"line\",\n" +
                "          \"fromX\": 1,\n" +
                "          \"toX\": 0,\n" +
                "          \"startFrame\": 0,\n" +
                "          \"duration\": 23\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"line\",\n" +
                "          \"fromY\": 0,\n" +
                "          \"toY\": 1,\n" +
                "          \"startFrame\": 23,\n" +
                "          \"duration\": 23\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"line\",\n" +
                "          \"fromX\": 0,\n" +
                "          \"toX\": 1,\n" +
                "          \"fromY\": 1,\n" +
                "          \"toY\": 1,\n" +
                "          \"startFrame\": 46,\n" +
                "          \"duration\": 23\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"line\",\n" +
                "          \"fromX\": 1,\n" +
                "          \"toX\": 1,\n" +
                "          \"fromY\": 1,\n" +
                "          \"toY\": 0,\n" +
                "          \"startFrame\": 78,\n" +
                "          \"duration\": 36,\n" +
                "          \"interpolator\": \"elegantlySlowOut\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }"

        val obj = json.decodeFromString(MediaSerializer, testJson)

        println(obj)

        val backToJson = json.encodeToString(MediaSerializer, obj)

        println(backToJson)
    }

    @Test
    fun testMediaGroup() {

        val testJson = "{\n" +
                "      \"textureIndex\": 0,\n" +
                "      \"type\": \"group\",\n" +
                "      \"id\": \"groupImages\",\n" +
                "      \"width\": \"match_parent\",\n" +
                "      \"height\": \"match_parent\",\n" +
                "      \"medias\": [\n" +
                "        {\n" +
                "          \"textureIndex\": 0,\n" +
                "          \"type\": \"image\",\n" +
                "          \"demoSource\": \"https://images.unsplash.com/photo-1531853121101-cb94c8ed218d?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80\",\n" +
                "          \"width\": \"840/1080w\",\n" +
                "          \"height\": \"840/1080w\",\n" +
                "          \"x\": \"39\",\n" +
                "          \"y\": \"86\",\n" +
                "          \"anchorX\": \"end\",\n" +
                "          \"anchorY\": \"bottom\",\n" +
                "          \"startFrame\": 40,\n" +
                "          \"animatorsIn\": [\n" +
                "            {\n" +
                "              \"type\": \"scale_inner\",\n" +
                "              \"duration\": 50,\n" +
                "              \"from\": 1,\n" +
                "              \"to\": 1.1\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"textureIndex\": 0,\n" +
                "          \"type\": \"image\",\n" +
                "          \"demoSource\": \"https://images.unsplash.com/photo-1587393795320-6e43b260ecd0?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80\",\n" +
                "          \"width\": \"617/1080w\",\n" +
                "          \"height\": \"706/1080w\",\n" +
                "          \"x\": \"94\",\n" +
                "          \"y\": \"338\",\n" +
                "          \"startFrame\": 20,\n" +
                "          \"animatorsIn\": [\n" +
                "            {\n" +
                "              \"type\": \"scale_inner\",\n" +
                "              \"duration\": 50,\n" +
                "              \"from\": 1,\n" +
                "              \"to\": 1.08\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }"

        val obj = json.decodeFromString(MediaGroupSerializer, testJson)

        println(obj)

        val backToJson = json.encodeToString(MediaGroupSerializer, obj)

        println(backToJson)

        val backToObj = json.decodeFromString(MediaGroupSerializer, backToJson)

        print(backToObj)
    }

    @Test
    fun testTemplate() {

        val testJson = "{\n" +
                "  \"preferredDuration\": 180,\n" +
                "  \"palette\": {\n" +
                "    \"backgroundImage\": \"https://images.unsplash.com/photo-1527954513726-611b208be16a?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=80\"\n" +
                "  },\n" +
                "  \"medias\": [\n" +
                "    {\n" +
                "      \"type\": \"text\",\n" +
                "      \"text\": \"Create text styles\",\n" +
                "      \"anchorX\": \"center\",\n" +
                "      \"innerGravity\": \"start\",\n" +
                "      \"width\": \"0.691w\",\n" +
                "      \"height\": \"0.172h\",\n" +
                "      \"translationX\": -0.0343,\n" +
                "      \"translationY\": 0.0463,\n" +
                "      \"startFrame\": 5,\n" +
                "      \"minDuration\": \"as_template\",\n" +
                "      \"font\": {\n" +
                "        \"fontPath\": \"spectral\",\n" +
                "        \"fontStyle\": \"bold\"\n" +
                "      },\n" +
                "      \"textSize\": \"0.047m\",\n" +
                "      \"textColor\": \"#ffffff\",\n" +
                "      \"lineSpacing\": 0.7,\n" +
                "      \"animationParamIn\": {\n" +
                "        \"wordDelay\": 5,\n" +
                "        \"lineDelay\": 5,\n" +
                "        \"textAnimators\": [\n" +
                "          {\n" +
                "            \"type\": \"fade\",\n" +
                "            \"from\": 0,\n" +
                "            \"to\": 1,\n" +
                "            \"duration\": 10,\n" +
                "            \"interpolator\": \"elegantlySlowOut\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"type\": \"move_to_y\",\n" +
                "            \"from\": 1,\n" +
                "            \"to\": 0,\n" +
                "            \"duration\": 10,\n" +
                "            \"interpolator\": \"elegantlySlowOut\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}"

        val template = json.decodeFromString<Template>(testJson)

        println(template)

        val backToJson = json.encodeToString(template)

        println(backToJson)

        val backToTemplate = json.decodeFromString<Template>(backToJson)

        print(backToTemplate)
    }


    @Test
    fun testMediaVector() {

        val testJson = """
            {
          "textureIndex": 2,
          "id": "vectorOverlay1",
          "type": "vector",
          "width": "867",
          "height": "1000",
          "y": "72",
          "x": "0",
          "anchorX": "end",
          "isMovable": false,
          "isLoopEnabled": false,
          "originalSource": "file:///android_asset/template-resources/love/Love2Messages/contur_1.json",
          "minDuration": "as_template",
          "startFrame": 3,
          "colorFilter": "#ff000000"
        }
        """.trimIndent()

        val obj = json.decodeFromString(MediaSerializer, testJson)

        println(obj)

        val backToJson = json.encodeToString(MediaSerializer, obj)

        println(backToJson)
    }
}
