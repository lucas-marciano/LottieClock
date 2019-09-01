package com.lucasmarciano.lottieclock

import android.app.Application
import android.content.Intent
import android.os.SystemClock
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.schedulers.Schedulers

class TrueTimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Thread {
            kotlin.run {
                SystemClock.sleep(5000)

                /**
                 * Sempre iniciaremos a busca por alguma data TrueTime, pois
                 * como o tick do aparelho é que será utilizado, ainda temos
                 * a possibilidade de ter uma data não atualizada se não houver
                 * uma nova conexão com a Internet. Mas lembrando que uma nova
                 * invocação a servidor NTP não remove da cache a TrueTime já
                 * salva.
                 * */
                TrueTimeRx
                    .build()
                    .withSharedPreferencesCache(this)
                    .initializeRx("time.apple.com")
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        {
                            val intent = Intent(BroadcastApplication.FILTER)

                            LocalBroadcastManager
                                .getInstance(this@TrueTimeApplication)
                                .sendBroadcast(intent)
                        },
                        {
                            it.printStackTrace()
                        }
                    )
            }
        }.start()


    }
}