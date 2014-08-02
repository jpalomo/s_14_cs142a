package types;

public class ArrayType extends Type {
    
    private Type base;
    private int extent;
    
    public ArrayType(int extent, Type base)
    {
        this.extent = extent;
        this.base = base;
        throw new RuntimeException("implement operators");
    }
    
    public int extent()
    {
        return extent;
    }
    
    public Type base()
    {
        return base;
    }
    
    @Override
    public String toString() {
		//String s = "array[" + extent "," + base.toString() + "]";
			String s = null;
		return s;
    }
    
    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof IntType))
            return false;
        
        ArrayType aType = (ArrayType)that;
        return this.extent == aType.extent && base.equivalent(aType.base);
    }

	@Override
	public Type assign(Type source) {
		return base.assign(source);
	}
	
	@Override
    public Type index(Type index) {
        if (index.equivalent(new IntType()) ) {
			return new AddressType(base.index(index));
        } 

		return super.index(index);
    }
}
