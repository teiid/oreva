package org.odata4j.expression;

import org.odata4j.expression.OrderByExpression.Direction;

public class AbstractExpressionVisitor implements ExpressionVisitor {

  @Override
  public void beforeDescend() {
  }

  @Override
  public void afterDescend() {
  }

  @Override
  public void betweenDescend() {
  }

  @Override
  public void visit(String type) {
  }

  @Override
  public void visit(OrderByExpression expr) {
  }

  @Override
  public void visit(Direction direction) {
  }

  @Override
  public void visit(AddExpression expr) {
  }

  @Override
  public void visit(AndExpression expr) {
  }

  @Override
  public void visit(BooleanLiteral expr) {
  }

  @Override
  public void visit(CastExpression expr) {
  }

  @Override
  public void visit(ConcatMethodCallExpression expr) {
  }

  @Override
  public void visit(DateTimeLiteral expr) {
  }

  @Override
  public void visit(DateTimeOffsetLiteral expr) {
  }

  @Override
  public void visit(DecimalLiteral expr) {
  }

  @Override
  public void visit(DivExpression expr) {
  }

  @Override
  public void visit(EndsWithMethodCallExpression expr) {
  }

  @Override
  public void visit(EntitySimpleProperty expr) {
  }

  @Override
  public void visit(EqExpression expr) {
  }

  @Override
  public void visit(GeExpression expr) {
  }

  @Override
  public void visit(GtExpression expr) {
  }

  @Override
  public void visit(GuidLiteral expr) {
  }

  @Override
  public void visit(BinaryLiteral expr) {
  }

  @Override
  public void visit(ByteLiteral expr) {
  }

  @Override
  public void visit(SByteLiteral expr) {
  }

  @Override
  public void visit(IndexOfMethodCallExpression expr) {
  }

  @Override
  public void visit(SingleLiteral expr) {
  }

  @Override
  public void visit(DoubleLiteral expr) {
  }

  @Override
  public void visit(IntegralLiteral expr) {
  }

  @Override
  public void visit(Int64Literal expr) {
  }

  @Override
  public void visit(IsofExpression expr) {
  }

  @Override
  public void visit(LeExpression expr) {
  }

  @Override
  public void visit(LengthMethodCallExpression expr) {
  }

  @Override
  public void visit(LtExpression expr) {
  }

  @Override
  public void visit(ModExpression expr) {
  }

  @Override
  public void visit(MulExpression expr) {
  }

  @Override
  public void visit(NeExpression expr) {
  }

  @Override
  public void visit(NegateExpression expr) {
  }

  @Override
  public void visit(NotExpression expr) {
  }

  @Override
  public void visit(NullLiteral expr) {
  }

  @Override
  public void visit(OrExpression expr) {
  }

  @Override
  public void visit(ParenExpression expr) {
  }

  @Override
  public void visit(BoolParenExpression expr) {
  }

  @Override
  public void visit(ReplaceMethodCallExpression expr) {
  }

  @Override
  public void visit(StartsWithMethodCallExpression expr) {
  }

  @Override
  public void visit(StringLiteral expr) {
  }

  @Override
  public void visit(SubExpression expr) {
  }

  @Override
  public void visit(SubstringMethodCallExpression expr) {
  }

  @Override
  public void visit(SubstringOfMethodCallExpression expr) {
  }

  @Override
  public void visit(TimeLiteral expr) {
  }

  @Override
  public void visit(ToLowerMethodCallExpression expr) {
  }

  @Override
  public void visit(ToUpperMethodCallExpression expr) {
  }

  @Override
  public void visit(TrimMethodCallExpression expr) {
  }

  @Override
  public void visit(YearMethodCallExpression expr) {
  }

  @Override
  public void visit(MonthMethodCallExpression expr) {
  }

  @Override
  public void visit(DayMethodCallExpression expr) {
  }

  @Override
  public void visit(HourMethodCallExpression expr) {
  }

  @Override
  public void visit(MinuteMethodCallExpression expr) {
  }

  @Override
  public void visit(SecondMethodCallExpression expr) {
  }

  @Override
  public void visit(RoundMethodCallExpression expr) {
  }

  @Override
  public void visit(FloorMethodCallExpression expr) {
  }

  @Override
  public void visit(CeilingMethodCallExpression expr) {
  }

  @Override
  public void visit(AggregateAnyFunction expr) {
  }

  @Override
  public void visit(AggregateAllFunction expr) {
  }

}
