// This file is automatically generated by tool/GenerateAst
import java.util.List

abstract class Expr {
    interface Visitor<R> {
        R visitCommandExpr(Command expr);
        R visitParamExpr(Param expr);
    }
    static class Command extends Expr{
        Command extends Expr {
            this.command = command;
            this.param = param;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCommandExpr(this);
        }

        final Token command;
        final Expr param;
    }

    abstract <R> r accept(Visitor<R> visitor);
    static class Param extends Expr{
        Param extends Expr {
            this.param = param;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitParamExpr(this);
        }

        final Token param;
    }

    abstract <R> r accept(Visitor<R> visitor);
}
