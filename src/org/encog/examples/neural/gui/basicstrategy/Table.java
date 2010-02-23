package org.encog.examples.neural.gui.basicstrategy;

import java.util.ArrayList;
import java.util.List;

public class Table {
	private Shoe shoe;
	private Dealer dealer;
	private List<Player> players = new ArrayList<Player>();
	
	public Table(int decks, Dealer dealer)
	{
		this.shoe = new Shoe(decks);
		this.dealer = dealer;
	}
	
	public int getNextCard()
	{
		if( shoe.remainingPercent()<0.1 )
			shoe.shuffle();
		return shoe.nextCard();
	}
	
	public void deal()
	{
		dealer.giveCard(getNextCard());
		for(Player player: this.players)
		{
			player.giveCard(getNextCard());
		}
	}
	
	public void resetAll()
	{
		this.dealer.reset(0.0);
		for(Player player: this.players)
		{
			player.takeMoney(5);			
			player.reset(5);
		}
	}
	
	public void play()
	{
		resetAll();
		deal();
		deal();				
		
		for(Player player: this.players)
		{
			playPlayer(player);
		}
		
		playPlayer(dealer);
		
		awardMoney();
		
		System.out.println( toString() );
	}
	
	public void awardMoney()
	{
		boolean dealerBusted = false;
		
		if( this.dealer.getCurrentHand().calculateTotal()>21 ) {
			dealerBusted = true;
		}
		
		int dealerPoints = this.dealer.getCurrentHand().calculateTotal();
		
		for(Player player: players) {
			for( Hand hand: player.getHands() ) {
				
				if( hand.isDone() )
					continue;
				
				int points = hand.calculateTotal();
				
				if( dealerBusted ) {
					player.giveMoney(hand.getBet()*2);
				}
				else if( points==dealerPoints )
				{
					player.giveMoney(hand.getBet());
				}
				else if( points>dealerPoints )
				{
					player.giveMoney(hand.getBet()*2);
					if( hand.isBlackJack() ) {
						player.giveMoney(hand.getBet()/2);
					}
				}
			}
		}
		
	}
	
	public void playPlayer(Participant player)
	{
		int currentHand = 0;
		
		while( currentHand<player.getHands().size())
		{
			Hand hand = player.getHands().get(currentHand);
			playHand(player, hand);
			currentHand++;
		}
	}
	
	public void playHand(Participant player, Hand hand)
	{
		boolean done = false;
		
		if( hand.isBlackJack() ) {			
			done = true;
		}
		
		while(!done)
		{
			// perform the action
			char action = player.play(this);
			switch(action)
			{
				case Participant.ACTION_DOUBLE:
					if( hand.getHandIndex()==2 ) {
					player.giveCard(getNextCard());
					((Player)player).takeMoney(hand.getBet());
					hand.setBet(hand.getBet()*2.0);
					done = true;
					}
					else
						player.giveCard(getNextCard());
					break;
				case Participant.ACTION_HIT:
					player.giveCard(getNextCard());
					break;
				case Participant.ACTION_SPLIT:
					Hand hand1 = player.getCurrentHand();
					Hand hand2 = player.createHand(hand.getBet());
					hand2.getHand()[0] = hand1.getHand()[1];
					hand1.getHand()[1] = getNextCard(); 
					hand2.getHand()[1] = getNextCard();	
					hand2.setHandIndex(2);
					((Player)player).takeMoney(hand.getBet());
					break;
				case Participant.ACTION_STAND:
					done = true;
					break;
				case Participant.ACTION_SURRENDER:
					hand.setDone(true);
					((Player)player).giveMoney(hand.getBet()/2.0);
					done = true;
					break;
			}	
			
			// bust?
			int total = hand.calculateTotal();
			
			if( total>21 )
			{
				hand.setDone(true);
				done = true;
			}
		}
	}

	public Shoe getShoe() {
		return shoe;
	}

	public Dealer getDealer() {
		return dealer;
	}

	public List<Player> getPlayers() {
		return players;
	}
	
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		result.append("Dealer:" );
		result.append(this.dealer.toString());
		result.append("\n");
		
		int playerNo = 1;
		
		for(Player player: this.players)
		{
			result.append("Player #" + playerNo + ":");
			result.append(player.toString());
			result.append("\n");
			playerNo++;
		}
		
		return result.toString();
	}

	public void addPlayer(Player player) {
		this.players.add(player);		
	}
	
}
