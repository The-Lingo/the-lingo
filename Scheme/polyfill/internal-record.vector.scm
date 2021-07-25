(define (?*record? x)
  (and (vector? x) (= (vector-length x) 3) (eq? (vector-ref x 0) '%%?*ReCoRd%%)))
(define (?*record name fields)
  (vector '%%?*ReCoRd%% name fields))
(define (?*record-name x)
  (if (?*record? x)
      (vector-ref x 1)
      (error "?*record-name" "not ?*record" x)))
(define (?*record-fields x)
  (if (?*record? x)
      (vector-ref x 2)
      (error "?*record-name" "not ?*record" x)))
(define (?*record-field-ref x i) (list-ref (?*record-fields x) i))
