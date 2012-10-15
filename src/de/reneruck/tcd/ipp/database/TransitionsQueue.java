package de.reneruck.tcd.ipp.database;

import java.util.LinkedList;
import java.util.List;

import de.reneruck.tcd.ipp.datamodel.Transition;
import de.reneruck.tcd.ipp.datamodel.TransitionState;

public class TransitionsQueue {

	private List<Transition> transitions = new LinkedList();
	
	public Transition getTransitionById(long transitionId) {
		for (Transition transition : this.transitions) {
			if(transition.getTransitionId() == transitionId)
			{
				return transition;
			}
		}
		return null;
	}
	
	public List<Transition> getAllTransitionsByState(TransitionState state)
	{
		List<Transition> result = new LinkedList<Transition>();
		for (Transition transition : this.transitions) {
			if(state.equals(transition.getTransitionState()))
			{
				result.add(transition);
			}
		}
		return result;
	}
	
	public void addTransition(Transition transition) {
		this.transitions.add(transition);
	}
	
	public void removeTransition(Transition transition) {
		this.transitions.remove(transition);
	}
	
	public void removeTransitionById(long transitionId) {
		Transition transitionById = getTransitionById(transitionId);
		if(transitionById != null) {
			removeTransition(transitionById);
		}
	}
}
