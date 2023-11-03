package com.example.cupcake.data

import android.graphics.Bitmap

data class RecResult(
    val bms:List<Bitmap>,
    var pers:List<Float>
)

fun getPollutionLevel(per:Float):Int{
    if(per<0.2) {
        return 0
    } else if(per>=0.2&&per<0.4) {
        return 1
    } else if(per>=0.4&&per<0.6) {
        return 2
    } else if(per>=0.6&&per<0.8) {
        return 3
    } else if(per>=0.8&&per<1.0) {
        return 4
    }
        //
    return 5
}