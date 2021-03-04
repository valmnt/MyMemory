package com.rkpandey.mymemory.models

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.rkpandey.mymemory.FlipAnimation
import com.rkpandey.mymemory.R
import com.rkpandey.mymemory.utils.DEFAULT_ICONS
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer
import kotlin.coroutines.coroutineContext

class MemoryGame(private val boardSize: BoardSize, customImages: List<String>?) {

  val cards: List<MemoryCard>
  var numPairsFound = 0

  private var numCardFlips = 0
  private var indexOfSingleSelectedCard: Int? = null

  init {
    if (customImages == null) {
      val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
      val randomizedImages = (chosenImages + chosenImages).shuffled()
      cards = randomizedImages.map { MemoryCard(it) }
    } else {
      val randomizedImages = (customImages + customImages).shuffled()
      cards = randomizedImages.map { MemoryCard(it.hashCode(), it) }
    }
  }

  fun flipCard(position: Int, view: View,context: Context, callback: ((Boolean) -> Unit)): Boolean {
    numCardFlips++
    val card = cards[position]
    var foundMatch = false
    // Three cases
    // 0 cards previously flipped over => restore cards + flip over the selected card
    // 1 card previously flipped over => flip over the selected card + check if the images match
    // 2 cards previously flipped over => restore cards + flip over the selected card
    if (indexOfSingleSelectedCard == null) {
      // 0 or 2 selected cards previously
      restoreCards()
      indexOfSingleSelectedCard = position
    } else {
      // exactly 1 card was selected previously
      foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)

      indexOfSingleSelectedCard = null
    }

    val animFadein: Animation = FlipAnimation(ImageView(context), Drawable.createFromPath(card.imageUrl) ,true)
    animFadein.setAnimationListener(object: Animation.AnimationListener {
      override fun onAnimationRepeat(animation: Animation) {
      }
      override fun onAnimationEnd(animation: Animation) {
        card.isFaceUp = !card.isFaceUp
        callback.invoke(foundMatch)
      }
      override fun onAnimationStart(animation: Animation) {
      }
    })
    view.startAnimation(animFadein)
    if(!card.isMatched){
      restoreCards()
      cards[position].isFaceUp = false
      if(indexOfSingleSelectedCard != null){
        cards[indexOfSingleSelectedCard!!].isFaceUp = false
      }
    }
    return foundMatch
  }

  private fun checkForMatch(position1: Int, position2: Int): Boolean {
    if (cards[position1].identifier != cards[position2].identifier) {
      return false
    }
    cards[position1].isMatched = true
    cards[position2].isMatched = true
    numPairsFound++
    return true
  }

  // Turn all unmatched cards face down
  fun restoreCards() {
    for (card in cards) {
      if (!card.isMatched) {
        card.isFaceUp = false
      }
    }
  }

  fun haveWonGame(): Boolean {
    return numPairsFound == boardSize.getNumPairs()
  }

  fun isCardFaceUp(position: Int): Boolean {
    return cards[position].isFaceUp
  }

  fun getNumMoves(): Int {
    return numCardFlips / 2
  }
}