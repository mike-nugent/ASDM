package loot;

import java.util.HashMap;

public class LootManager 
{
	//        super(gave_up, rolled_dice, rolled_highest, everyone_pass, has_acquired);

	static String lastPersonToRoll = null;
	static HashMap<String, Integer> rollMap = new HashMap<String, Integer>();
	
	public static void playerGaveUpRoll(String person)
	{
		System.out.println(person + " gave up rolling");
		rollMap.put(person, 0);
	}
	
	public static void playerRolledDice(String person, int roll, int limit)
	{
		System.out.println(person + " rolled " + roll + " out of " + limit);
		rollMap.put(person, roll);
		DicePopupPage.updateLastToRoll(person, roll);
	}
	
	public static void playerRolledHighest(String person)
	{
		System.out.println(person + " rolled the highest");
		int highest = 0;
		for(int next : rollMap.values())
		{
			if(highest < next)
			{
				highest = next;
			}
		}
		DicePopupPage.updateWinsRoll(person, highest);
		rollMap.clear();
	}
	
	public static void everyonePassed()
	{
		System.out.println("Everyone has passed");
		rollMap.clear();
	}
	
	public static void playerHasAcquiredItem(String person, int totalNumItems, String itemID)
	{
		System.out.println(person + " has acquired " + totalNumItems + " of " + itemID); 
	}
}
