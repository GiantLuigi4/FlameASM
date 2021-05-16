package tfc.flameasm.remapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class MappingsSteps implements Iterable<MappingsInfo>, Iterator<MappingsInfo> {
	private final ArrayList<String> completedSteps = new ArrayList<>();
	protected final ArrayList<String> steps = new ArrayList<>();
	
	protected void addStep(String name) {
	}
	
	public boolean hasNext() {
		return !steps.isEmpty();
	}
	
	public void reset() {
		completedSteps.addAll(steps);
		steps.clear();
		steps.addAll(completedSteps);
		completedSteps.clear();
	}
	
	@Override
	public Iterator<MappingsInfo> iterator() {
		return this;
	}
	
	@Override
	public void forEach(Consumer<? super MappingsInfo> action) {
		while (hasNext()) {
			action.accept(next());
		}
	}
	
	@Override
	public Spliterator<MappingsInfo> spliterator() {
		return new Spliterator<MappingsInfo>() {
			@Override
			public boolean tryAdvance(Consumer action) {
				//noinspection unchecked
				action.accept(next());
				return hasNext();
			}
			
			@Override
			public Spliterator<MappingsInfo> trySplit() {
				return this;
			}
			
			@Override
			public long estimateSize() {
				return steps.size();
			}
			
			@Override
			public int characteristics() {
				return
						Spliterator.IMMUTABLE |
								Spliterator.ORDERED |
								Spliterator.SIZED |
								Spliterator.DISTINCT |
								Spliterator.CONCURRENT |
								Spliterator.NONNULL |
								Spliterator.SUBSIZED
						;
			}
		};
	}
	
	@Override
	public MappingsInfo next() {
		String step = steps.remove(0);
		completedSteps.add(step);
		return MappingApplicator.getInfo(step);
	}
}
