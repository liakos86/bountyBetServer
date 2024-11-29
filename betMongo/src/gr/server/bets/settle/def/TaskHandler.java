package gr.server.bets.settle.def;

import java.util.Set;

public interface TaskHandler<T> {


	boolean handle(Set<T> toHandle) throws Exception;
	
}
