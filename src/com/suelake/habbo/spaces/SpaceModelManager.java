package com.suelake.habbo.spaces;

import java.util.Vector;

import com.blunk.Environment;
import com.blunk.Log;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.items.Item;

public class SpaceModelManager
{
	private Vector<SpaceModel> m_models;
	
	public SpaceModelManager()
	{
		m_models = new Vector<SpaceModel>();
	}
	
	@SuppressWarnings("unchecked")
	public void loadModels()
	{
		m_models.clear();
		Log.info("Loading space models...");
		
		// Load models
		SpaceModelLoader modelLoader = (SpaceModelLoader)HabboHotel.getDataQueryFactory().newQuery("SpaceModelLoader");
		m_models = (Vector<SpaceModel>)Environment.getDatabase().query(modelLoader);
		
		// Load passive objects for each model
		SpaceModelObjectsLoader objLoader = (SpaceModelObjectsLoader)HabboHotel.getDataQueryFactory().newQuery("SpaceModelObjectsLoader");
		for (SpaceModel model : m_models)
		{
			objLoader.modelType = model.type;
			Vector<Item> objs = (Vector<Item>)Environment.getDatabase().query(objLoader);
			model.setPassiveObjects(objs);
		}
		
		Log.info("Loaded " + m_models.size() + " space models.");
	}
	
	public SpaceModel getModel(String type)
	{
		for (SpaceModel model : m_models)
		{
			if (model.type.equals(type))
			{
				return model;
			}
		}
		
		// Model not found!
		Log.error("SpaceModelManager could not find SpaceModel '" + type + "'!");
		return null;
	}
}
