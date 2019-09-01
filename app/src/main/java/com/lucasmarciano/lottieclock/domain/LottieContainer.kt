package com.lucasmarciano.lottieclock.domain

import android.animation.Animator
import android.content.Context
import com.airbnb.lottie.LottieAnimationView
import com.lucasmarciano.lottieclock.ClockActivity
import java.util.*

/**
 * Classe criada para ser responsável por manter toda a
 * gerência da animação principal de ClockActivity.
 * */
class LottieContainer(private val context: Context, private val animation: LottieAnimationView) :
    Animator.AnimatorListener {

    /**
     * Constantes que contém valores que possivelmente seriam
     * alterados com frequência em projeto de produção. Essas
     * constantes evitam que trabalhemos com valores mágicos,
     * algo negativo na arquitetura limpa de projeto.
     * */
    companion object {
        const val SPEED_SUN_TO_MOON = 1.5F
        const val SPEED_MOON_TO_SUN = -1.5F
        const val FRAME_FIRST = 46
        const val FRAME_LAST = 82
    }

    var hour: Int = 0

    /**
     * A importância do bloco de inicialização aqui é que a
     * animação sempre estará com os dados iniciais corretos e
     * componentes que têm mudança brusca de cor já estarão
     * com as cores corretas.
     * */
    init {
        animation.setMinAndMaxFrame(FRAME_FIRST, FRAME_LAST)
        animation.addAnimatorListener(this)

        startHourAndColor()
    }


    /**
     * Método que deve ser invocado no bloco inicial da classe
     * (init{}) afim de obter a atual hora presente no device
     * e assim definir também a cor dos componentes da
     * ClockActivity que permitem a mudança brusca de cor.
     * */
    private fun startHourAndColor() {
        val calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR_OF_DAY)

        updateActivityViewsColor()
    }

    private fun updateActivityViewsColor() {
        (context as ClockActivity)
            .setViewsColorByTime(isMorning(hour))
    }

    /**
     * Não há problemas em manter aqui os valores mágicos 6 e 18,
     * pois essa é a definição de "dia (sol)" para o nosso domínio
     * de problema.
     * */
    private fun isMorning(hour: Int): Boolean =
        hour in 6..17

    /**
     * Método responsável por verificar se a hora atualmente
     * definida, de acordo com a seleção em Spinner, exige que a
     * animação aconteça, ou seja, se for uma hora que indica "dia"
     * e a animação atualmente esteja em "noite", então a animação
     * de "noite" para "dia" deve ocorrer e vice-versa.
     * */
    private fun canAnimate(hour: Int): Boolean =
        (isMorning(hour) && animation.frame == FRAME_LAST)
                || (!isMorning(hour) && animation.frame == FRAME_FIRST)

    /**
     * Método responsável pela verificação e então a ativação de
     * animação, se necessária. Deve ser invocado sempre que uma
     * nova hora for fornecida pelo sistema de horário construído
     * para o aplicativo.
     * */
    fun updateByHour(hour: Int) {
        if (!canAnimate(hour)) {
            return
        }
        var speedValue = SPEED_SUN_TO_MOON

        if (isMorning(hour)) {
            speedValue = SPEED_MOON_TO_SUN
        }

        this.hour = hour
        animation.speed = speedValue
        animation.playAnimation()

        updateActivityViewsColor()
    }

    override fun onAnimationStart(animator: Animator?) {}
    override fun onAnimationEnd(animator: Animator?) {
        animation.frame =
            if (isMorning(hour))
                FRAME_FIRST
            else
                FRAME_LAST
    }

    override fun onAnimationRepeat(animator: Animator?) {}
    override fun onAnimationCancel(animator: Animator?) {}
}