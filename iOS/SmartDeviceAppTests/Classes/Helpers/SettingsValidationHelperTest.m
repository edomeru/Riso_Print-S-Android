/ / 
 / /     S e t t i n g s V a l i d a t i o n H e l p e r T e s t . m 
 / /     S m a r t D e v i c e A p p 
 / / 
 / /     C r e a t e d   b y   A m o r   C o r a z o n   R i o   o n   3 / 2 8 / 1 4 . 
 / /     C o p y r i g h t   ( c )   2 0 1 4   a L i n k .   A l l   r i g h t s   r e s e r v e d . 
 / / 
 
 # i m p o r t   < G H U n i t I O S / G H U n i t . h > 
 # i m p o r t   " S e t t i n g s V a l i d a t i o n H e l p e r . h " 
 
 # d e f i n e   T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C   @ " 1 2 3 4 5 6 7 8 9 0 A B C D E F G H I J K L M N O P Q R S T U V W X Y Z a b c d e f g h i j k l m n o p q r s t u v w x y z " 
 
 @ i n t e r f a c e   S e t t i n g s V a l i d a t i o n H e l p e r T e s t   :   G H T e s t C a s e {   } 
 @ e n d 
 @ i m p l e m e n t a t i o n   S e t t i n g s V a l i d a t i o n H e l p e r T e s t 
 { 
         
 } 
 -   ( v o i d )   t e s t S h o u l d A c c e p t C a r d I D I n p u t _ L e s s T h a n 1 2 8 C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ] ; 
         B O O L   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   s h o u l d A c c e p t C a r d I D I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t T r u e ( r e s u l t ,   n i l ) ; 
 } 
 
 - ( v o i d )   t e s t S h o u l d A c c e p t C a r d I D I n p u t _ M o r e T h a n 1 2 8 C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ] ; / / 6 2 C h a r s 
         [ t e s t S t r i n g   a p p e n d S t r i n g : T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ] ; / / + 6 2 C h a r s 
         [ t e s t S t r i n g   a p p e n d S t r i n g :   [ t e s t S t r i n g   s u b s t r i n g T o I n d e x : 5 ] ] ;   / / + 5 C h a r s 
         B O O L   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   s h o u l d A c c e p t C a r d I D I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t F a l s e ( r e s u l t ,   n i l ) ; 
 } 
 
 - ( v o i d )   t e s t S h o u l d A c c e p t C a r d I D I n p u t _ 1 2 8 C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ] ; / / 6 2 C h a r s 
         [ t e s t S t r i n g   a p p e n d S t r i n g : T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ] ; / / + 6 2 C h a r s 
         [ t e s t S t r i n g   a p p e n d S t r i n g :   [ t e s t S t r i n g   s u b s t r i n g T o I n d e x : 4 ] ] ;   / / + 4 C h a r s 
         B O O L   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   s h o u l d A c c e p t C a r d I D I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t T r u e ( r e s u l t ,   n i l ) ; 
 } 
 
 - ( v o i d )   t e s t S h o u l d A c c e p t C a r d I D I n p u t _ 0 C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : @ " " ] ; 
         B O O L   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   s h o u l d A c c e p t C a r d I D I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t T r u e ( r e s u l t ,   n i l ) ; 
 } 
 
 -   ( v o i d )   t e s t S h o u l d A c c e p t C o m m u n i t y N a m e I n p u t _ L e s s T h a n 1 5 C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : @ " a b c d e f g h i j k l m n " ] ; 
         B O O L   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   s h o u l d A c c e p t C o m m u n i t y N a m e I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t T r u e ( r e s u l t ,   n i l ) ; 
 } 
 
 - ( v o i d )   t e s t S h o u l d A c c e p t C o m m u n i t y N a m e I n p u t _ M o r e T h a n 1 5 C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : @ " A B C D E F G H I J K L M N O P " ] ; 
         B O O L   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   s h o u l d A c c e p t C o m m u n i t y N a m e I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t F a l s e ( r e s u l t ,   n i l ) ; 
 } 
 
 - ( v o i d )   t e s t S h o u l d A c c e p t C o m m u n i t y N a m e I n p u t _ 1 5 C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : @ " 0 1 2 3 4 5 6 7 8 9 A B C D E " ] ; 
         B O O L   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   s h o u l d A c c e p t C o m m u n i t y N a m e I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t T r u e ( r e s u l t ,   n i l ) ; 
 } 
 
 - ( v o i d )   t e s t V a l i d a t e C a r d I D I n p u t _ V a l i d C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h S t r i n g : T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ] ; 
         k S e t t i n g s I n p u t E r r o r   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   v a l i d a t e C a r d I D I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t T r u e ( ( r e s u l t   = =   k S e t t i n g s I n p u t E r r o r N o n e )   ,   n i l ) ; 
 } 
 
 - ( v o i d )   t e s t V a l i d a t e C a r d I D I n p u t _ I n v a l i d C h a r s 
 { 
         
         N S S t r i n g   * i n v a l i d C h a r s   = @ "   + " ; / / o t h e r   c h a r a c t e r s   r u l e d   o u t   b y   k e y b o a r d 
         f o r ( i n t   i   =   0 ;   i   <   i n v a l i d C h a r s . l e n g t h ;   i + + ) 
         { 
                 u n i c h a r   i n v a l i d C h a r   =   [ i n v a l i d C h a r s   c h a r a c t e r A t I n d e x : i ] ; 
                 N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h F o r m a t : @ " % @ % c " ,   T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ,   i n v a l i d C h a r ] ; 
                 k S e t t i n g s I n p u t E r r o r   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   v a l i d a t e C a r d I D I n p u t : t e s t S t r i n g ] ; 
                 G H A s s e r t T r u e ( ( r e s u l t   = =   k S e t t i n g s I n p u t E r r o r I n v a l i d C a r d I D )   ,   [ N S S t r i n g   s t r i n g W i t h F o r m a t : @ " I n p u t   w i t h   i n v a l i d   c h a r : % c " ,   i n v a l i d C h a r ] ) ; 
         } 
 } 
 
 - ( v o i d )   t e s t V a l i d a t e C o m m u n i t y N a m e I n p u t _ V a l i d C h a r s 
 { 
         N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h F o r m a t : @ " % @ % @ " , T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ,   @ " - / : ; ( ) $ & @ . , ? ! [ ] { } % ^ * + = _ | ~ < > � � � " . , ? ! " ] ; 
 
         k S e t t i n g s I n p u t E r r o r   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   v a l i d a t e C o m m u n i t y N a m e I n p u t : t e s t S t r i n g ] ; 
         G H A s s e r t T r u e ( ( r e s u l t   = =   k S e t t i n g s I n p u t E r r o r N o n e )   ,   [ N S S t r i n g   s t r i n g W i t h F o r m a t : @ " T e s t   d a t a :   % @ " ,   t e s t S t r i n g ] ) ; 
 } 
 
 - ( v o i d )   t e s t V a l i d a t e C o m m u n i t y N a m e I n p u t _ I n v a l i d C h a r s 
 { 
         N S S t r i n g   * i n v a l i d C h a r s   = @ "   \ \ ' \ " # " ; 
         f o r ( i n t   i   =   0 ;   i   <   i n v a l i d C h a r s . l e n g t h ;   i + + ) 
         { 
                 u n i c h a r   i n v a l i d C h a r   =   [ i n v a l i d C h a r s   c h a r a c t e r A t I n d e x : i ] ; 
                 N S M u t a b l e S t r i n g   * t e s t S t r i n g   =   [ N S M u t a b l e S t r i n g   s t r i n g W i t h F o r m a t : @ " % @ % c " ,   T E S T _ D A T A _ A L L _ E N G L I S H _ A L P H A N U M E R I C ,   i n v a l i d C h a r ] ; 
                 k S e t t i n g s I n p u t E r r o r   r e s u l t   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   v a l i d a t e C o m m u n i t y N a m e I n p u t : t e s t S t r i n g ] ; 
                 G H A s s e r t T r u e ( ( r e s u l t   = =   k S e t t i n g s I n p u t E r r o r C o m m u n i t y N a m e I n v a l i d C h a r s )   ,   [ N S S t r i n g   s t r i n g W i t h F o r m a t : @ " I n p u t   w i t h   i n v a l i d   c h a r : % c " ,   i n v a l i d C h a r ] ) ; 
         } 
 } 
 
 - ( v o i d )   t e s t E r r o r M e s s a g e 
 { 
         N S A r r a y   * e r r o r M e s s a g e s   =   [ N S A r r a y   a r r a y W i t h O b j e c t s :   @ " " , 
                                                                                                                 @ " C a r d   I D   s h o u l d   b e   a l p h a n u m e r i c   o n l y " , 
                                                                                                                 @ " I n p u t   r e q u i r e d   f o r   c o m m u n i t y   n a m e " , 
                                                                                                                 @ " C o m m u n i t y   N a m e   s h o u l d   n o t   c o n t a i n :   \ \ ' \ " # " , 
                                                                                                                 n i l ] ; 
         
         f o r ( i n t   i   =   0 ;   i   <   e r r o r M e s s a g e s . c o u n t ;   i + + ) 
         { 
         
                 N S S t r i n g   * e x p e c t e d M s g   =   ( N S S t r i n g   * ) [ e r r o r M e s s a g e s   o b j e c t A t I n d e x : i ] ; 
                 N S S t r i n g   * a c t u a l M s g   =   [ S e t t i n g s V a l i d a t i o n H e l p e r   e r r o r M e s s a g e F o r S e t t i n g s I n p u t E r r o r : i ] ; 
                 N S S t r i n g   * d e s c   =   [ N S S t r i n g   s t r i n g W i t h F o r m a t : @ " E r r o r   m e s s a g e   f o r   k S e t t i n g s I n p u t E r r o r [ % d ]   =   % @ \ n   E x p e c t e d : % @ " ,   i ,   a c t u a l M s g ,   e x p e c t e d M s g ] ; 
                 G H A s s e r t T r u e ( [ e x p e c t e d M s g   i s E q u a l T o S t r i n g : a c t u a l M s g ] ,   d e s c ) ; 
         } 
 } 
 @ e n d 
 