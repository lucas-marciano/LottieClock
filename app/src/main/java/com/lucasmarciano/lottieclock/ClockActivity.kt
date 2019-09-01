package com.lucasmarciano.lottieclock

import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lucasmarciano.lottieclock.domain.LottieContainer
import kotlinx.android.synthetic.main.activity_clock.*
import java.util.*

class ClockActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var countriesGmt: Array<String>
    lateinit var broadcast: BroadcastApplication
    lateinit var lottieContainer: LottieContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        initBroadcastReceiver()

        countriesGmt = resources.getStringArray(R.array.countries_gmt)

        sp_countries.onItemSelectedListener = this
        lottieContainer = LottieContainer(this, lav_sun_moon)
    }

    /**
     * Método responsável por registrar um BroadcastReceiver
     * (BroadcastApplication) para poder receber uma comunicação
     * de TrueTimeApplication, comunicação sobre o retorno de
     * uma data / horário corretos de algum servidor NTP.
     * */
    private fun initBroadcastReceiver() {
        broadcast = BroadcastApplication(this)
        val filter = IntentFilter(BroadcastApplication.FILTER)

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(broadcast, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(broadcast)
    }

    /**
     * Listener de novo item selecionado em Spinner. Note que
     * este método é sempre invocado quando a atividade é
     * construída, pois o item inicial em Spinner é considerado
     * um "novo item selecionado", dessa forma desde o início
     * a TrueTime API será solicitada sem que nós
     * desenvolvedores tenhamos de criar algum código somente
     * para essa invocação inicial.
     * */
    override fun onItemSelected(
        adapter: AdapterView<*>?,
        itemView: View?,
        position: Int,
        id: Long
    ) {
        AsyncTrueTime(this)
            .execute(countriesGmt[position])
    }

    override fun onNothingSelected(adapter: AdapterView<*>?) {}

    /**
     * Método responsável por apresentar / esconder a View de informação
     * sobre a origem do horário (GMT) sendo utilizado: servidor NTP
     * (certeza que o horário estará correto); ou aparelho. Este método
     * será invocado sempre no doInBackground() de uma instância de
     * AsyncTrueTime, por isso a necessidade do runOnUiThread() para que
     * a atualização de View não seja fora da Thread UI.
     * */
    fun infoDateShow(status: Boolean) {
        runOnUiThread {
            ll_info_date.visibility =
                if (status)
                    View.VISIBLE
                else
                    View.INVISIBLE
        }
    }

    /**
     * Método responsável por atualizar tanto o ClockImageView
     * quanto o TextView de horário de acordo com o parâmetro
     * Calendar fornecido. Este método será invocado sempre no
     * onPostExecute() de uma instância de AsyncTrueTime.
     * */
    fun updateClock(trueTime: Calendar) {

        val hour = trueTime.get(Calendar.HOUR_OF_DAY)
        val minute = trueTime.get(Calendar.MINUTE)

        civ_clock.animateToTime(hour, minute)
        tv_clock.text = String.format("%02d:%02d", hour, minute)

        lottieContainer.updateByHour(hour)
    }

    /**
     * Método que invocará onItemSelected() para atualizar o
     * horário, isso, pois fireSpinnerItemSelected() somente
     * será acionado assim que a API TrueTime tiver retorno de
     * algum servidor NTP. fireSpinnerItemSelected() garante
     * que o horário em apresentação é o correto.
     * */
    fun fireSpinnerItemSelected() {

        sp_countries
            .onItemSelectedListener!!
            .onItemSelected(null, null, sp_countries.selectedItemPosition, 0)
    }

    /**
     * Permite a mudança de cor dos componentes visuais da
     * ClockActivity que precisam acompanhar o design de acordo
     * com a animação em tela.
     * */
    fun setViewsColorByTime(isMorning: Boolean) {
        if (isMorning) {
            sp_countries.setPopupBackgroundResource(R.color.colorPrimary)
        } else {
            sp_countries.setPopupBackgroundResource(R.color.colorAccent)
        }
    }

}