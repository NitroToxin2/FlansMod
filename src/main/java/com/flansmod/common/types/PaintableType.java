package com.flansmod.common.types;

import java.util.ArrayList;

import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.Paintjob;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class PaintableType extends InfoType
{
	//Paintjobs
	/** The list of all available paintjobs for this gun */
	public ArrayList<Paintjob> paintjobs = new ArrayList<Paintjob>();
	/** The default paintjob for this gun. This is created automatically in the load process from existing info */
	public Paintjob defaultPaintjob;	
	/** Assigns IDs to paintjobs */
	private int nextPaintjobID = 1;
	
	public PaintableType(TypeFile file)
	{
		super(file);
	}

	@Override
	public void postRead(TypeFile file)
	{
		//After all lines have been read, set up the default paintjob
		defaultPaintjob = new Paintjob(0, "", texture, new ItemStack[0]);
		//Move to a new list to ensure that the default paintjob is always first
		ArrayList<Paintjob> newPaintjobList = new ArrayList<Paintjob>();
		newPaintjobList.add(defaultPaintjob);
		newPaintjobList.addAll(paintjobs);
		paintjobs = newPaintjobList;
		if(infoTypes.containsKey(shortName.hashCode()))
		{
			FlansMod.Assert(false, "Duplicate info type name " + shortName);
		}
		
		// Add all custom paintjobs to dungeon loot. Equal chance for each
		totalDungeonChance += dungeonChance * (paintjobs.size() - 1);
	}
	
	/** Pack reader */
	protected void read(String[] split, TypeFile file)
	{
		super.read(split, file);
		try
		{
			//Paintjobs
			if(split[0].toLowerCase().equals("paintjob"))
			{
				ItemStack[] dyeStacks = new ItemStack[(split.length - 3) / 2];
				for(int i = 0; i < (split.length - 3) / 2; i++)
					dyeStacks[i] = new ItemStack(Items.dye, Integer.parseInt(split[i * 2 + 4]), getDyeDamageValue(split[i * 2 + 3]));
				if(split[1].contains("_"))
				{
					String[] splat = split[1].split("_");
					if(splat[0].equals(iconPath))
						split[1] = splat[1];
				}
				paintjobs.add(new Paintjob(nextPaintjobID++, split[1], split[2], dyeStacks));
			}
		} 
		catch (Exception e)
		{
			FlansMod.log("Reading file failed : " + shortName);
			e.printStackTrace();
		}
	}
	
	public Paintjob getPaintjob(String s)
	{
		for(Paintjob paintjob : paintjobs)
		{
			if(paintjob.iconName.equals(s))
				return paintjob;
		}
		return defaultPaintjob;
	}
	
	public Paintjob getPaintjob(int i)
	{
		return paintjobs.get(i);
	}
	
	@Override
	public void addDungeonLoot() 
	{
		if(dungeonChance > 0)
		{
			for(int i = 0; i < paintjobs.size(); i++)
			{
				ItemStack stack = new ItemStack(this.item);
				NBTTagCompound tags = new NBTTagCompound();
				tags.setString("Paint", paintjobs.get(i).iconName);
				stack.setTagCompound(tags);
				
				addToRandomChest(stack, (float)(FlansMod.dungeonLootChance * dungeonChance) / (float)totalDungeonChance);
			}
		}
	}

	public float GetRecommendedScale()
	{
		return 50.0f;
	}
}
