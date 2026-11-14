# Wesley — UML relationships

Codex chat transcript · Restaurant Rush

### User

How should I represent the fact that `Order` stores a `Dish`, which could be a `MenuItem` or a `ComboMeal`? What does each of the association and realization lines tell the reader? Give me the clues to look for in the code so I can decide which relationship to draw

### Codex

Look at **the field in** `Order` and **the declarations of the two meal classes**. They answer different questions.

- **Association tells the reader what an object holds a reference to.** In `Order.java`, find `private final Dish meal;`. Follow the `meal` constructor parameter to see what gets stored in that field. Use the field’s **declared type** to decide which classifier belongs at the other end of the solid association line.

- **Realization tells the reader which class implements an interface.** Look for `class MenuItem implements Dish` and `class ComboMeal implements Dish`. The `implements` keyword is your clue to use a dashed line with a hollow triangle pointing toward the interface.

To check your drawing, trace it as a reader. **What type does `Order` store? Which classes can supply an object of that type?** The association answers the first question, and the realization lines answer the second.

### User

What should I look for in the code to decide whether `GameEngine`’s relationship with `Waiter` is composition? How is that different from `Chef` using an `Order` when preparing food? Give me the clues to look for in the code so I can decide which relationship to draw.

### Codex

**For `GameEngine` and `Waiter`, look for ownership.** In `GameEngine.java`, find `private final Waiter waiter = new Waiter();`. Each engine creates its own waiter and keeps it as a field for the game. Search for the other uses of `waiter` and check whether the engine shares that object or uses it internally. A field tells you there’s a lasting reference. Creation, exclusive ownership and lifetime are the clues that support **composition**. The filled diamond goes at the owner’s end.

**For `Chef` and `Order`, follow the method parameter.** The engine looks up an existing order and passes it to `chef.prepare(order, turn)`. Inside `Chef`, `Order` appears as a parameter, and the method calls `order.prepare(turn)`. The chef uses the order for that action. Even though the call changes the order’s state, the code shows a **dependency**. A dashed arrow points from the class that uses the type toward the type it uses.

### User

A `ComboMeal` refers to two existing `MenuItem` objects that can also be sold individually. Is shared aggregation justified here, or would a plain association express the relationship clearly enough? Give me the clues to look for in the code so I can decide which relationship to draw

### Codex

Both are valid. A **plain association** shows that `ComboMeal` references existing `MenuItem` objects. **Shared aggregation** adds emphasis that those items are parts of the combo and can also exist independently.

Decide whether the **whole–part meaning** adds something you want the reader to notice.
