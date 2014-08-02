package types;

public class FuncType extends Type {
   
   private TypeList args;
   private Type ret;
   
   public FuncType(TypeList args, Type returnType)
   {
      this.args = args;
      this.ret = returnType;
      //throw new RuntimeException("implement operators");
   }
   
   public Type returnType()
   {
      return ret;
   }
   
   public TypeList arguments()
   {
      return args;
   }
   
   @Override
   public String toString()
   {
      return "func(" + args + "):" + ret;
   }

   @Override
   public boolean equivalent(Type that)
   {
      if (that == null)
         return false;
      if (!(that instanceof FuncType))
         return false;
      
      FuncType aType = (FuncType)that;
      return this.ret.equivalent(aType.ret) && this.args.equivalent(aType.args);
   }

   @Override
	public Type call(Type args)
   	{
		TypeList argsTL = (TypeList) args;
		//all function arguments should be of type TypeList 
		if (args instanceof TypeList) {
			if(argsTL.equivalent(this.args)){
				return returnType();
			}
			//type lists were not equivalent, throw an error
       		return new ErrorType("Cannot call " + this + " using " + args + ".");
        } 

		//we werent passed a TypeList, call the error message in super class
       	return super.call(args);
    }
}
