# DTEff (CoreDependentTypeEffect)

## Inspirations

+ https://github.com/koka-lang/koka
+ Coq
+ Idris / Agda

## Design

Effect: meta informations for functions

Values themself don't have effects

Examples:

+ sized
+ with assumptions
+ gradual typing
+ may not halt (div)
+ io
+ st

`n * sized n -> pure`?

`Type` is sized. How about `Effect`?
`Effect : Type 0` `div : Effect`

`Effect = Type -> Type` or a builtin feature?

### Halting

Take care of io. It is very easy to implement recursive function with io/st.

io/st implies div?

letrec -- data (maybe including closures) / function only

funtion only letrec can easily use `sized`. What about data? be lazy and mark them as `div`?

Is there any other ways to ensure halting? force Weak head normal form when doing recursive.


### Core language

Should the core language include the complete Effect system? or just a few special effects as `Type -> Type` (`sized` for instance)?
